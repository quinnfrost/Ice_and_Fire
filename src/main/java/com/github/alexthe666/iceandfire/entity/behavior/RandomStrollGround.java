package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.RandomStroll;

public class RandomStrollGround<E extends PathfinderMob & IBehaviorApplicable> extends RandomStroll {

    public RandomStrollGround(float pSpeedModifier, boolean pMayStrollFromWater) {
        super(pSpeedModifier, pMayStrollFromWater);
    }
}
