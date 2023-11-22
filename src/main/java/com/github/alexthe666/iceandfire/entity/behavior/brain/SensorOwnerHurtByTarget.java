package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class SensorOwnerHurtByTarget extends Sensor<TamableAnimal> {
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    public int timestamp;
    @Override
    protected void doTick(ServerLevel pLevel, TamableAnimal pEntity) {
        if (pEntity.isTame()) {
            LivingEntity owner = pEntity.getOwner();
            LivingEntity protectToAttack = owner.getLastHurtByMob();
            if (protectToAttack != null
                    && owner.getLastHurtByMobTimestamp() != this.timestamp
                    && pEntity.wantsToAttack(protectToAttack, pEntity.getOwner())) {
                pEntity.getBrain().setMemory(DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET, protectToAttack);
                this.timestamp = owner.getLastHurtByMobTimestamp();
            }
        }
        pEntity.getBrain().getMemory(DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET).ifPresent(livingEntity -> {
            if (!livingEntity.isAlive() || livingEntity.level != pLevel) {
                pEntity.getBrain().eraseMemory(DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET);
            }
        });
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }
}
