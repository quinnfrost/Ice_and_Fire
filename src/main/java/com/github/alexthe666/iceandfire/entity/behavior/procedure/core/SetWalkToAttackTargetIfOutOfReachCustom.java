package com.github.alexthe666.iceandfire.entity.behavior.procedure.core;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.function.Function;

@Deprecated
public class SetWalkToAttackTargetIfOutOfReachCustom<E extends Mob & IFlyableBehavior> extends Behavior<E> {
    private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;
    private final Function<LivingEntity, Float> speedModifier;
    private final MemoryModuleType<LivingEntity> targetHoldingMemory;
    public SetWalkToAttackTargetIfOutOfReachCustom(float pSpeedModifier) {
        this((p_147908_) -> {
            return pSpeedModifier;
        }, MemoryModuleType.ATTACK_TARGET);
    }

    public SetWalkToAttackTargetIfOutOfReachCustom(Function<LivingEntity, Float> pSpeedModifier, MemoryModuleType<LivingEntity> targetHoldingMemory) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, targetHoldingMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED));
        this.speedModifier = pSpeedModifier;
        this.targetHoldingMemory = targetHoldingMemory;
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        LivingEntity livingentity = pEntity.getBrain().getMemory(targetHoldingMemory).get();
        if (BehaviorUtils.canSee(pEntity, livingentity) && BehaviorUtils.isWithinAttackRange(pEntity, livingentity, 1)) {
            this.clearWalkTarget(pEntity);
        } else {
            this.setWalkAndLookTarget(pEntity, livingentity);
        }
    }

    protected void setWalkAndLookTarget(LivingEntity pEntity, LivingEntity pTarget) {
        Brain<?> brain = pEntity.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(pTarget, true));
        WalkTarget walktarget = new WalkTarget(new EntityTracker(pTarget, false), this.speedModifier.apply(pEntity), 0);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walktarget);
    }

    protected void clearWalkTarget(LivingEntity p_24036_) {
        p_24036_.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}
