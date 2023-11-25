package com.github.alexthe666.iceandfire.entity.behavior.core;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class Perch<E extends Mob & IBehaviorApplicable> extends Behavior<E> {

    public Perch() {
        this(150, 250);
    }
    public Perch(int pMinDuration, int pMaxDuration) {
        super(ImmutableMap.of(
//                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
//                MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT,
//                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT
        ), pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return !pOwner.canMove();
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return !pEntity.canMove();
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {

    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        pOwner.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }
}
