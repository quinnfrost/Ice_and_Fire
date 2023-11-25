package com.github.alexthe666.iceandfire.entity.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作为原来GoalSelector的替代，这样方便把原来的AI移植过来<br>
 * 每个{@link GoalSelector#tick()}：<br>
 * GoalCleanup阶段：<br>
 * 1. 对正在运行的Goal检查disabledFlags和canContinueToUse<br>
 * 2. 处理lockedFlags（先不管）<br>
 * GoalUpdate阶段：<br>
 * 1. 为每个Goal检查disabledFlags, lockedFlags和canUse<br>
 * 2. 终止其他Goals
 * 3. 调用start<br>
 * 4. 调用tick<br>
 * （原生Behavior的互斥好难做啊）
 * @param <E>
 */
public class RunInterruptable<E extends Mob> extends Behavior<E> {
    private final Set<Behavior<? super E>> behaviors = Sets.newLinkedHashSet();
    private Behavior<? super E> runningBehavior = null;

    public RunInterruptable(List<Behavior<? super E>> pBehaviors) {
        super(ImmutableMap.of());
        pBehaviors.forEach(behavior -> {
            behaviors.add(behavior);
        });
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return true;
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return true;
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {

    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        this.runningBehavior = null;
        for (Behavior<? super E> behavior : this.behaviors) {
            // Before the running behavior is confirmed
            if (this.runningBehavior == null) {
                // Try to start all behaviors that is prior to the current behavior
                if (behavior.getStatus() == Status.STOPPED && behavior.tryStart(pLevel, pOwner, pGameTime)) {
                    this.runningBehavior = behavior;
                } else if (behavior.getStatus() == Status.RUNNING) {
                    this.runningBehavior = behavior;
                }
            } else {
                // Stop all behaviors that is after the current behavior
                if (behavior.getStatus() == Status.RUNNING) {
                    behavior.doStop(pLevel, pOwner, pGameTime);
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        behaviors.forEach(behavior -> behavior.doStop(pLevel, pEntity, pGameTime));
    }

    @Override
    public String toString() {
        return "RunInterruptable{" +
                "runningBehavior=" + runningBehavior +
                '}';
    }
}
