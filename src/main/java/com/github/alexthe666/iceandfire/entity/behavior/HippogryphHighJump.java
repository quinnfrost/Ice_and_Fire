package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonFlightUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

public class HippogryphHighJump<E extends EntityHippogryph & IBehaviorApplicable> extends FlightScene<E>{
    public HippogryphHighJump() {
        super(ImmutableMap.of(

        ), 200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return DragonFlightUtils.canAreaSeeSky(pLevel, pOwner.getOnPos(),
                                               (int) Math.ceil(pOwner.getBbWidth())
        ) && super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return ImmutableSet.of(MemoryModuleType.ATTACK_TARGET).stream().noneMatch((memoryModuleType) -> {
            return pEntity.getBrain().hasMemoryValue(memoryModuleType);
        }) && super.canStillUse(pLevel, pEntity, pGameTime);
    }

    @Override
    protected Queue<Pair<Vec3, Integer>> generateFlightPath(ServerLevel pLevel, E pEntity) {
        BlockPos blockPos = DragonUtils.getBlockInViewHippogryph(pEntity, 0);
        if (blockPos == null) {
            return null;
        }

        Queue<Pair<Vec3, Integer>> flightPath = new ArrayDeque<>();
        flightPath.offer(Pair.of(pEntity.getPosition(1.0f), 10));
        flightPath.offer(Pair.of(Vec3.atCenterOf(blockPos.atY(pEntity.getOnPos().getY() + 50)), 60));
        flightPath.offer(Pair.of(pEntity.getPosition(1.0f).add(0,50,0), 40));
        flightPath.offer(Pair.of(pEntity.getPosition(1.0f), 10));

        return flightPath;
    }

}
