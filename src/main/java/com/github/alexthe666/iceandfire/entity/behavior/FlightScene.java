package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IAllMethodINeed;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class FlightScene<E extends Mob & IAllMethodINeed> extends Behavior<E> {
    protected boolean isLoop;
    protected int pathIndex = 0;
    protected Queue<Pair<Vec3, Integer>> flightPath;
    protected Vec3 currentFlightPoint;
    protected int currentFlightPointStayTime;
    protected long arrivedTimestamp = 0;
    protected int maxBetweenPointsTimeout;

    public FlightScene() {
        this(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 200);
    }

    public FlightScene(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int maxDuration) {
        super(pEntryCondition);
        this.maxBetweenPointsTimeout = maxDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        if (pOwner.canMove() && pOwner.canFly() && pOwner.getAirborneState() == DragonBehaviorUtils.AirborneState.GROUNDED) {
            this.flightPath = this.generateFlightPath(pLevel, pOwner);
            return this.flightPath != null && !this.flightPath.isEmpty();
        }
        return false;
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.flightPath = new ArrayDeque<>();
        this.currentFlightPoint = null;
        this.currentFlightPointStayTime = 0;
        this.pathIndex = 0;
        this.arrivedTimestamp = pGameTime;
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        Pair<Vec3, Integer> flightPoint = this.flightPath.poll();
        if (flightPoint == null) {
            this.doStop(pLevel, pEntity, pGameTime);
            return;
        }
        if (this.isLoop) {
            this.flightPath.offer(flightPoint);
        }
        this.currentFlightPoint = flightPoint.getFirst();
        this.currentFlightPointStayTime = flightPoint.getSecond();
        this.arrivedTimestamp = pGameTime;
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.currentFlightPoint == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        Optional<WalkTarget> optionalWalkTarget = pOwner.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (optionalWalkTarget.isEmpty() || optionalWalkTarget.get().getTarget().currentPosition().distanceToSqr(currentFlightPoint) > 1) {
            pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.currentFlightPoint, 1.0f, 0));
        }

        if (!this.hasArriveCurrentPoint(pLevel, pOwner, pGameTime)) {
            this.arrivedTimestamp = pGameTime + this.currentFlightPointStayTime;
        }
        if (this.shouldAdvance(pLevel, pOwner, pGameTime)) {
            this.start(pLevel, pOwner, pGameTime);
        }
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return pGameTime > this.arrivedTimestamp + maxBetweenPointsTimeout;
    }

    protected abstract Queue<Pair<Vec3, Integer>> generateFlightPath(ServerLevel pLevel, E pEntity);

    protected boolean shouldAdvance(ServerLevel pLevel, E pEntity, long pGameTime) {
        return this.arrivedTimestamp < pGameTime && this.hasArriveCurrentPoint(pLevel, pEntity, pGameTime);
    }

    protected boolean hasArriveCurrentPoint(ServerLevel pLevel, E pEntity, long pGameTime) {
        return DragonBehaviorUtils.isNavigatorArrived(pEntity, new BlockPos(this.currentFlightPoint));
    }

}
