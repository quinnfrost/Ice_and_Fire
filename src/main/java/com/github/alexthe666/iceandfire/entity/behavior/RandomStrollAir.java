package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonFlightUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class RandomStrollAir<E extends PathfinderMob & IBehaviorApplicable> extends RandomStroll {
    public RandomStrollAir(float pSpeedModifier) {
        super(pSpeedModifier);
    }

    public RandomStrollAir(float pSpeedModifier, boolean pMayStrollFromWater) {
        super(pSpeedModifier, pMayStrollFromWater);
    }

    public RandomStrollAir(float pSpeedModifier, int pMaxHorizontalDistance, int pMaxVerticalDistance) {
        super(pSpeedModifier, pMaxHorizontalDistance, pMaxVerticalDistance);
    }

    public RandomStrollAir(float pSpeedModifier, int pMaxHorizontalDistance, int pMaxVerticalDistance, boolean pMayStrollFromWater) {
        super(pSpeedModifier, pMaxHorizontalDistance, pMaxVerticalDistance, pMayStrollFromWater);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
        if (pOwner instanceof IBehaviorApplicable && ((E) pOwner).getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
            return false;
        }
        return super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Nullable
    protected Vec3 getTargetPos(PathfinderMob pPathfinder) {
        if (pPathfinder instanceof IBehaviorApplicable) {
            Vec3 randomPos = AirAndWaterRandomPos.getPos(
                    pPathfinder,
                    this.maxHorizontalDistance,
                    this.maxVerticalDistance,
                    ((E) pPathfinder).getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED ? 2 : 0,
                    0,
                    0,
                    (double) ((float) Math.PI / 2F)
            );
            if (randomPos == null || !DragonBehaviorUtils.shouldHoverAt(pPathfinder,
                                                                        new WalkTarget(randomPos, 1.0f, 0)
            )) {
                return null;
            }
            return randomPos;
        }
        return null;
    }

    protected int getPreferredFlyingLevel(E pPathfinder) {
        return Mth.ceil(DragonFlightUtils.getGround(pPathfinder.level, pPathfinder.getOnPos()).getY() + 5.0f * pPathfinder.getBbHeight());
    }

}
