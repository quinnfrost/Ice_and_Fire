package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class SensorDragonTest extends Sensor<Mob> {
    @Override
    protected void doTick(ServerLevel pLevel, Mob pEntity) {

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }
}
