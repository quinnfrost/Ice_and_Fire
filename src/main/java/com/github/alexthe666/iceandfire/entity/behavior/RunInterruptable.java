package com.github.alexthe666.iceandfire.entity.behavior;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class RunInterruptable<E extends Mob> extends Behavior<E> {
    public RunInterruptable(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition);
    }

    private final ImmutableList<Behavior<? super E>> behaviors = ImmutableList.of();
}
