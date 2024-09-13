package com.github.alexthe666.iceandfire.entity.behavior.core;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nullable;
import java.util.Optional;

public class FlyAndHover<E extends Mob & IBehaviorApplicable> extends Behavior<E> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    // Path stuck cool down
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;
    private AdvancedPathNavigate navigator;
    private boolean shouldLandOnTheWay = false;

    public FlyAndHover() {
        this(150, 250);
    }

    public FlyAndHover(int pMinDuration, int pMaxDuration) {
        super(ImmutableMap.of(
                DragonMemoryModuleType.FORBID_FLYING, MemoryStatus.REGISTERED,

                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, MemoryStatus.REGISTERED
        ), pMinDuration, pMaxDuration);
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        Brain<?> brain = pOwner.getBrain();
        WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
        // Have a target and we haven't there already
        if (!DragonBehaviorUtils.hasArrived(pOwner, walktarget.getTarget().currentBlockPosition(), 2.0d)) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            return pOwner.getNavigation() instanceof AdvancedPathNavigate
                    && pOwner.canMove()
                    && this.shouldUseFlyingNavigator(pOwner);
        }

        return false;
    }

    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.lastTargetPos != null) {
            Optional<WalkTarget> optional = pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET);

            return this.shouldUseFlyingNavigator(pEntity)
                    && optional.isPresent()
//                    && !DragonBehaviorUtils.reachedTarget(pEntity, optional.get())
                    && pEntity.canMove();
        } else {
            return false;
        }
    }

    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        // If stuck
        if (pEntity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !DragonBehaviorUtils.reachedTarget(pEntity, pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && pEntity.getNavigation().isStuck()) {
            this.remainingCooldown = pLevel.getRandom().nextInt(40);
        }

        pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            if (DragonBehaviorUtils.reachedTarget(pEntity, walkTarget)) {
                if (!DragonBehaviorUtils.shouldHoverAt(pEntity, walkTarget) && pEntity.canLand()) {
                    pEntity.land();
                } else {
                    pEntity.hover();
                }
                pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                pEntity.getBrain().eraseMemory(MemoryModuleType.PATH);
                pEntity.getBrain().eraseMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE);
            }
        });
        this.path = null;
        this.navigator = null;
    }

    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.navigator = (AdvancedPathNavigate) pEntity.getNavigation();

        pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            shouldLandOnTheWay = DragonBehaviorUtils.shouldHoverAt(pEntity, walkTarget) && pEntity.getAirborneState() == DragonBehaviorUtils.AirborneState.FLY;
            if (DragonBehaviorUtils.shouldFlyToTarget(pEntity, walkTarget)) {
                pEntity.takeoff();
            }
            pEntity.flightTo(walkTarget);
        });

    }

    /**
     * Manage flying and hovering behavior. Their only difference is in move controller <br>
     * Flying move control are faster, speeds up when diving, and angle clamped <br>
     * Hovering move control are slow, and is able to actual hover in the air <br>
     * TODO: none of these are implemented yet <br>
     * Use {@link IBehaviorApplicable#setAirborneState(DragonBehaviorUtils.AirborneState)} to control controller movement <br>
     * This will also affect animation
     * @param pLevel
     * @param pOwner
     * @param pGameTime
     */
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        Path path = pOwner.getNavigation().getPath();
        Brain<?> brain = pOwner.getBrain();

        if (this.path == null && this.navigator.getPath() != null) {
            this.path = this.navigator.getPath();
        }

        pOwner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            // Target is over-air
            if (DragonBehaviorUtils.shouldHoverAt(pOwner, walkTarget)
                    && DragonBehaviorUtils.reachedTarget(pOwner, walkTarget)) {
                pOwner.setAirborneState(DragonBehaviorUtils.AirborneState.HOVER);
            }
            // Target is on ground
            else {
                if (!DragonBehaviorUtils.shouldHoverAt(pOwner, new WalkTarget(pOwner, 1.0f, 0))
                        && !DragonBehaviorUtils.shouldHoverAt(pOwner, walkTarget)
                        && pOwner.getAirborneState() == DragonBehaviorUtils.AirborneState.FLY
                        && pOwner.canLand()) {
                    pOwner.land();
                }
            }

            // Arrive and hover
            if (this.path != null && !this.path.canReach() && this.navigator.isDone()) {
                this.start(pLevel, pOwner, pGameTime);
            }
            if (this.lastTargetPos != null) {
                if (walkTarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D) {
                    this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
                    this.start(pLevel, pOwner, pGameTime);
                    pOwner.setAirborneState(DragonBehaviorUtils.AirborneState.FLY);
                }

            }

        });
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return super.timedOut(pGameTime);
    }

    protected boolean shouldUseFlyingNavigator(E entity) {
        return !entity.getBrain().getMemory(DragonMemoryModuleType.FORBID_FLYING).orElse(false)
                && (entity.getAirborneState() != DragonBehaviorUtils.AirborneState.GROUNDED || !entity.canLand());
    }

}
