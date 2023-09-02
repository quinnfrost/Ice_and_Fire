package com.github.alexthe666.iceandfire.entity.behavior.procedure;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.RandomStroll;

public class RandomStrollGround<E extends PathfinderMob & IFlyableBehavior> extends RandomStroll {

    public RandomStrollGround(float pSpeedModifier, int pMaxHorizontalDistance, int pMaxVerticalDistance, boolean pMayStrollFromWater) {
        super(pSpeedModifier, pMaxHorizontalDistance, pMaxVerticalDistance, pMayStrollFromWater);
    }
}
