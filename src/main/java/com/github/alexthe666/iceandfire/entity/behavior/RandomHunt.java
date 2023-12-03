package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Function;

public class RandomHunt<E extends PathfinderMob & IBehaviorApplicable> extends Behavior<E> {
    protected Function<E, Optional<LivingEntity>> targetSupplier;
    public RandomHunt(Function<E, Optional<LivingEntity>> pTargetSupplier) {
        super(ImmutableMap.of(DragonMemoryModuleType.NEAREST_HUNTABLE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryStatus.VALUE_ABSENT));
//        super(ImmutableMap.of(MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryStatus.VALUE_ABSENT));
        this.targetSupplier = pTargetSupplier;
    }

    public RandomHunt() {
        this((entity) -> {
            return entity.getBrain().getMemory(DragonMemoryModuleType.NEAREST_HUNTABLE);
        });
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return pOwner.getRandom().nextInt(100) < 90 && super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
//        pEntity.getBrain().setActiveActivityIfPossible(DragonActivity.HUNT);
        targetSupplier.apply(pEntity).ifPresent((entity) -> {
            pEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, entity);
        });
    }
}
