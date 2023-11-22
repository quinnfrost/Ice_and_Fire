package com.github.alexthe666.iceandfire.entity.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.function.Function;

public class FollowAdultTamed<E extends TamableAnimal> extends BabyFollowAdult<E> {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public FollowAdultTamed() {
        this(ADULT_FOLLOW_RANGE, livingEntity -> 0.6f);
    }
    public FollowAdultTamed(UniformInt pFollowRange, Function<LivingEntity, Float> pSpeedModifier) {
        super(pFollowRange, pSpeedModifier);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return checkShouldFollow(pOwner) && super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return checkShouldFollow(pEntity) && super.canStillUse(pLevel, pEntity, pGameTime);
    }

    private boolean checkShouldFollow(E pEntity) {
        if (pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).isPresent()) {
            return false;
        }
        Optional<AgeableMob> adult = pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT);
        if (pEntity.isTame() && (!pEntity.getOwner().equals(adult.get()))) {
            return false;
        }
        return true;
    }
}
