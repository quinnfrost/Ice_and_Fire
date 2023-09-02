package com.github.alexthe666.iceandfire.entity.behavior.procedure;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public abstract class FlightFollowing<E extends Mob & IFlyableBehavior> extends Behavior<E> {
    public static Vec3 debug;
    public static final boolean CLOCKWISE = true;
    protected int nextPosUpdateInterval;
    protected Vec3 followCenter;
    protected Vec3 nextPosition;

    public FlightFollowing(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int nextPosUpdateInterval) {
        super(pEntryCondition);
        this.nextPosUpdateInterval = nextPosUpdateInterval;
    }
    public FlightFollowing(int nextPosUpdateInterval) {
        this(ImmutableMap.of(), nextPosUpdateInterval);
    }
    public FlightFollowing() {
        this(ImmutableMap.of(), 1);
    }

    protected abstract Vec3 getCenter(ServerLevel pLevel, E pEntity, long pGameTime);

    protected abstract Vec3 getNextPosition(E pEntity, Vec3 center, float radius);

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.followCenter = null;
        this.nextPosition = null;
    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        this.followCenter = this.getCenter(pLevel, pOwner, pGameTime);
        if (this.followCenter == null) {
            this.doStop(pLevel, pOwner, pGameTime);
        }
        if (!(pGameTime % this.nextPosUpdateInterval == this.nextPosUpdateInterval - 1)) {
            return;
        }
        this.nextPosition = this.getNextPosition(pOwner, this.followCenter, 5);
        if (this.nextPosition != null) {
            debug = this.nextPosition;
            pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.nextPosition, 1.5f, 0));
        }
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return false;
    }

}
