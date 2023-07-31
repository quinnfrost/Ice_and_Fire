package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.AdultSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Optional;
import java.util.Set;

public class SensorNearestAdultTamed extends Sensor<TamableAnimal> {
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    protected void doTick(ServerLevel pLevel, TamableAnimal pEntity) {
        pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((p_186145_) -> {
            this.setNearestVisibleAdult(pEntity, p_186145_);
        });
    }

    private void setNearestVisibleAdult(TamableAnimal tamed, NearestVisibleLivingEntities pNearbyEntities) {
        Optional<AgeableMob> optional = pNearbyEntities.findClosest((livingEntity) -> {
            return livingEntity.getType() == tamed.getType()
                    && !livingEntity.isBaby()
                    && (!tamed.isTame()
                    || (livingEntity instanceof TamableAnimal tamedAdult
                    && tamed.getOwner().equals(tamedAdult.getOwner())));
        }).map(AgeableMob.class::cast);
        tamed.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
    }
}
