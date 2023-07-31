package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IAllMethodINeed;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAt<E extends Mob & IAllMethodINeed> extends Behavior<E> {
    public LookAt(int pMinDuration, int pMaxDuration) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return pOwner.canMove();
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return pEntity.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((positionTracker) -> {
            return positionTracker.isVisibleBy(pEntity);
        }).isPresent()
                && checkExtraStartConditions(pLevel, pEntity);
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        pOwner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_23486_) -> {
            pOwner.getLookControl().setLookAt(p_23486_.currentPosition());
        });
    }

}
