package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.pathfinding.NodeProcessorFly;
import com.github.alexthe666.iceandfire.pathfinding.NodeProcessorWalk;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathPointExtended;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathResult;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathingStuckHandler;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.PathJobFlyToLocation;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.PathJobMoveToLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Take care of walking, flying, and hovering navigation <br>
 * This navigator pre-process the flying path to determine whether 1.direct path is available 2.whether a simple 2 node path is available <br>
 * If not, use the super method that use node based pathfinding
 */
public class NavigatorFrostDragon<E extends EntityDragonBase & IBehaviorApplicable> extends AdvancedPathNavigate {
    public enum MovementStage {
        GROUND_STAY,
        GROUND_MOVE,
        HOVER_STAY,
        HOVER_MOVE,
        FLIGHT
    }

    protected MovementType movementType;
    protected E dragon;
    protected Vec3 targetPosition;
    protected double speedFactor;

    public NavigatorFrostDragon(E entity, Level world, MovementType type, float width, float height, PathingStuckHandler stuckHandler) {
        super(entity, world, type, width, height, stuckHandler);
        this.movementType = type;
        this.dragon = entity;
    }

    public NavigatorFrostDragon(E entity, Level world, MovementType type, float width, float height) {
        this(entity,
             world,
             type,
             width,
             height,
             PathingStuckHandler.createStuckHandler().withTeleportSteps(6).withTeleportOnFullStuck()
        );
    }

    public void switchMovementType(MovementType type) {
        stop();
        switch (type) {
            case FLYING:
                this.nodeEvaluator = new NodeProcessorFly();
                getPathingOptions().setIsFlying(true);
                break;
            case WALKING:
                this.nodeEvaluator = new NodeProcessorWalk();
                break;
            case CLIMBING:
                this.nodeEvaluator = new NodeProcessorWalk();
                getPathingOptions().setCanClimb(true);
                break;
        }
    }

    public MovementType getMovementType() {
        return movementType;
    }

    @Override
    protected void trimPath() {
        super.trimPath();
    }

    /**
     * @param x           the x target.
     * @param y           the y target.
     * @param z           the z target.
     * @param speedFactor the speed to walk.
     * @return
     */
    @Nullable
    @Override
    public PathResult moveToXYZ(double x, double y, double z, double speedFactor) {
        if (movementType == MovementType.WALKING) {
            return super.moveToXYZ(x, y, z, speedFactor);
        } else if (movementType == MovementType.FLYING) {
            final int newX = Mth.floor(x);
            final int newY = (int) y;
            final int newZ = Mth.floor(z);

            if (pathResult != null && pathResult.getJob() instanceof PathJobMoveToLocation &&
                    (
                            pathResult.isComputing()
                                    || (destination != null && isEqual(destination, newX, newY, newZ))
                                    || (originalDestination != null && isEqual(originalDestination, newX, newY, newZ))
                    )
            ) {
                return pathResult;
            }

            final BlockPos start = getPathingOptions().isFlying() ? ourEntity.blockPosition() : AbstractPathJob.prepareStart(
                    ourEntity);
            BlockPos desiredPos = new BlockPos(newX, newY, newZ);

            return setPathJob(
                    new PathJobMoveToLocation(ourEntity.level,
                                              start,
                                              desiredPos,
                                              (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
                                              ourEntity
                    ),
                    desiredPos, speedFactor, true
            );
        } else {
            IceAndFire.LOGGER.warn("Illegal movement type: " + movementType);
            return new PathResult();
        }
    }

    /**
     * The one moveTo to rule them all
     *
     * @param targetPosition
     * @param speedFactor
     */
    public void moveTo(Vec3 targetPosition, double speedFactor) {
        this.targetPosition = targetPosition;
        this.speedFactor = speedFactor;

        updateAirboneState();
        updateMovementType();

        moveToXYZ(targetPosition.x, targetPosition.y, targetPosition.z, speedFactor);

    }

    private void updateMovementType() {
        switch (dragon.getAirborneState()) {
            case GROUNDED:
                movementType = MovementType.WALKING;
                break;
            case TAKEOFF:
            case HOVER:
            case LANDING:
            case FLY:
                movementType = MovementType.FLYING;
                break;
        }
    }

    private void updateAirboneState() {
        switch (dragon.getAirborneState()) {
            case GROUNDED:
                if (!(dragon.getMoveControl() instanceof MoveControlFrostDragon.GroundMoveControl)) {
                    dragon.setMoveControl(new MoveControlFrostDragon.GroundMoveControl(dragon));
                    switchMovementType(MovementType.WALKING);
                }
                break;
            case TAKEOFF:
            case HOVER:
            case LANDING:
                if (!(dragon.getMoveControl() instanceof MoveControlFrostDragon.HoverMoveControl)) {
                    dragon.setMoveControl(new MoveControlFrostDragon.HoverMoveControl(dragon));
                    switchMovementType(MovementType.FLYING);
                }
                break;
            case FLY:
                if (!(dragon.getMoveControl() instanceof MoveControlFrostDragon.FlyMoveControl)) {
                    dragon.setMoveControl(new MoveControlFrostDragon.FlyMoveControl(dragon));
                    switchMovementType(MovementType.FLYING);
                }
                break;
        }
    }

    /**
     * Fly the dragon.
     * If the target is distance:
     * 1. target is in sight: direct flight by moveController
     * 2. a mountain is in the way: add two nodes higher/lower to try fly over, by moveController
     * 3. use node base navigator
     * TODO: simplify path nodes?
     */
    @Override
    public void tick() {
        if (targetPosition == null) {
            return;
        }
        if (movementType == MovementType.WALKING) {
            super.tick();
        } else if (movementType == MovementType.FLYING) {
//            super.tick();
            dragon.getMoveControl().setWantedPosition(targetPosition.x,
                                                      targetPosition.y,
                                                      targetPosition.z,
                                                      speedFactor
            );
        } else {
            IceAndFire.LOGGER.warn("Illegal movement type: " + movementType);
        }

        updateAirboneState();
        updateMovementType();

    }

    @Override
    public void stop() {
        super.stop();
    }
}
