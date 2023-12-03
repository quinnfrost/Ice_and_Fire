package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class SensorOwnerHurtTarget extends Sensor<TamableAnimal> {
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    private int timestamp;
    @Override
    protected void doTick(ServerLevel pLevel, TamableAnimal pEntity) {
        if (pEntity.isTame() && pEntity.getOwner() != null) {
            LivingEntity owner = pEntity.getOwner();
            LivingEntity assistToAttack = owner.getLastHurtMob();
            if (assistToAttack != null
                    && owner.getLastHurtMobTimestamp() != this.timestamp
                    && pEntity.wantsToAttack(assistToAttack, pEntity.getOwner())) {
                pEntity.getBrain().setMemory(DragonMemoryModuleType.LAST_OWNER_HURT_TARGET, assistToAttack);
                this.timestamp = owner.getLastHurtMobTimestamp();
            }
        }
        pEntity.getBrain().getMemory(DragonMemoryModuleType.LAST_OWNER_HURT_TARGET).ifPresent(livingEntity -> {
            if (!livingEntity.isAlive() || livingEntity.level != pLevel) {
                pEntity.getBrain().eraseMemory(DragonMemoryModuleType.LAST_OWNER_HURT_TARGET);
            }
        });
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }

}
