package com.github.alexthe666.iceandfire.entity.behavior.procedure.core;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
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

public class WalkAndStay<E extends Mob & IFlyableBehavior> extends Behavior<E> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    // Path stuck cool down
    private int remainingCooldown;
    @Nullable
    private Path path;
    // For identifying new target
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;
    private AdvancedPathNavigate navigator;

    public WalkAndStay() {
        this(150, 250);
    }

    public WalkAndStay(int pMinDuration, int pMaxDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, MemoryStatus.REGISTERED
        ), pMinDuration, pMaxDuration);
    }

    // Whether behavior should start
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> brain = pOwner.getBrain();
            WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            // Have a target and we haven't there already
            if (!DragonBehaviorUtils.isNavigatorArrived(pOwner, walktarget.getTarget().currentBlockPosition())) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();

                return pOwner.canMove()
                        && this.shouldUseWalkingNavigator(pOwner);
            }
            // We're already there
            else {
                if (pOwner.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
                    brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                }
                return false;
            }
        }
    }

    // Can behavior continues. Called every AI tick
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.lastTargetPos != null) {
            Optional<WalkTarget> optional = pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            return this.shouldUseWalkingNavigator(pEntity)
                    && pEntity.canMove()
                    && !pEntity.getNavigation().isDone()
                    && optional.isPresent()
                    && !DragonBehaviorUtils.reachedTarget(pEntity, optional.get());
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
                pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                pEntity.getBrain().eraseMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE);
            }
        });
        this.path = null;
        this.navigator = null;
    }

    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.navigator = (AdvancedPathNavigate) pEntity.getNavigation();

        pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            if (DragonBehaviorUtils.shouldHoverAt(pEntity, walkTarget) && pEntity.canFly()) {
                pEntity.takeoff();
            } else {
                pEntity.walkTo(walkTarget);
            }
        });
    }

    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        if (pLevel.isEmptyBlock(pOwner.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentBlockPosition())
                && DragonBehaviorUtils.isNavigatorFailedToReachTarget(pOwner) && pOwner.canFly()) {
            pOwner.takeoff();
            return;
        }

        if (this.path == null && this.navigator.getPath() != null) {
            this.path = this.navigator.getPath();
        }

        Brain<?> brain = pOwner.getBrain();
        if (this.lastTargetPos != null) {
            WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                this.start(pLevel, pOwner, pGameTime);
            }

        }
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return super.timedOut(pGameTime);
    }

    protected boolean shouldUseWalkingNavigator(E entity) {
        return !entity.getBrain().getMemory(DragonMemoryModuleType.FORBID_WALKING).orElse(false) &&
                (entity.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED || !entity.canFly());
    }
}
