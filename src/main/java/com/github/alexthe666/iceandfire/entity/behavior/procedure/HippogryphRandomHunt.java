package com.github.alexthe666.iceandfire.entity.behavior.procedure;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;

@Deprecated
public class HippogryphRandomHunt<E extends EntityHippogryph & IFlyableBehavior> extends StartAttacking<E> {
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
