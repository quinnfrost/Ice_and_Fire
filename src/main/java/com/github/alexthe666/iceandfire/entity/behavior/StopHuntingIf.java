package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.function.Consumer;

public class StopHuntingIf<E extends PathfinderMob & IBehaviorApplicable> extends Behavior<E> {
    private final Consumer<E> onTargetErased;
    private long huntStartTick;
    private int maxDuration;
    public StopHuntingIf(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, Consumer<E> pOnTargetErased, int maxDuration) {
        super(pEntryCondition);
        this.onTargetErased = pOnTargetErased;
        this.maxDuration = maxDuration;
    }

    public StopHuntingIf(Consumer<E> pOnTargetErased, int maxDuration) {
        this(ImmutableMap.of(), pOnTargetErased, maxDuration);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.huntStartTick == 0) {
            this.huntStartTick = pGameTime;
        } else {
            IceAndFire.LOGGER.warn("This should not happen");
        }
    }

    @Override
    protected boolean timedOut(long pGameTime) {
        return false;
    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {

    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return pGameTime - this.huntStartTick <= maxDuration || pEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (pGameTime > 0 && pGameTime - this.huntStartTick > maxDuration) {
            this.clearAttackTarget(pEntity);
            this.huntStartTick = 0;
        }
    }

    protected void clearAttackTarget(E pMemoryHolder) {
        this.onTargetErased.accept(pMemoryHolder);
        pMemoryHolder.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

        if (!pMemoryHolder.getBrain().hasMemoryValue(DragonMemoryModuleType.NEAREST_HUNTABLE)) {
            pMemoryHolder.getBrain().setActiveActivityIfPossible(Activity.IDLE);
//            pMemoryHolder.getBrain().updateActivityFromSchedule(pMemoryHolder.level.getDayTime(), pMemoryHolder.level.getGameTime());
        }
    }

}
