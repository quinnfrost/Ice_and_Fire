package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IAllMethodINeed;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.StartAttacking;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class HippogryphRandomHunt<E extends EntityHippogryph & IAllMethodINeed> extends StartAttacking<E> {
    public HippogryphRandomHunt() {
        super(
                e -> {
                    return !e.isTame();
                },
                e -> {
                    return e.getBrain().getMemory(DragonMemoryModuleType.NEAREST_HUNTABLE);
                }
        );
    }


}
