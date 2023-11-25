package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonActivity;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class HippogryphRandomHunt<E extends EntityHippogryph & IBehaviorApplicable> extends Behavior<E> {
    public HippogryphRandomHunt() {
        super(ImmutableMap.of(DragonMemoryModuleType.NEAREST_HUNTABLE, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().setActiveActivityIfPossible(DragonActivity.HUNT);
        pEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, pEntity.getBrain().getMemory(DragonMemoryModuleType.NEAREST_HUNTABLE).get());
    }
}
