package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@Deprecated
public class HurtByTarget <E extends Mob & IBehaviorApplicable> extends Behavior<E> {
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    public HurtByTarget() {
        this(new Class[0]);
    }

    public HurtByTarget(Class<?>... pToIgnoreDamage) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.REGISTERED
        ));
        this.toIgnoreDamage = pToIgnoreDamage;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        int i = pOwner.getLastHurtByMobTimestamp();
        LivingEntity revengeTarget = pOwner.getLastHurtByMob();
        if (/* pOwner.getLastHurtByMobTimestamp() == pOwner.tickCount + AI_TICK_TIMESTAMP_OFFSET */ i != this.timestamp && revengeTarget != null) {

            for (Class<?> oclass : this.toIgnoreDamage) {
                if (oclass.isAssignableFrom(revengeTarget.getClass())) {
                    return false;
                }
            }

            return pOwner.canAttack(revengeTarget, HURT_BY_TARGETING);
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, pEntity.getLastHurtByMob());
        this.timestamp = pEntity.getLastHurtByMobTimestamp();

        super.start(pLevel, pEntity, pGameTime);
    }
}
