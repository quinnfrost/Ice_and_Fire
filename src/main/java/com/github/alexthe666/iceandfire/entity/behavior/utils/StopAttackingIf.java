package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StopAttackingIf<E extends PathfinderMob & IBehaviorApplicable> extends StopAttackingIfTargetInvalid<E> {
    private final Predicate<E> stopAttackingIf;
    private final Consumer<E> onTargetErased;

    public StopAttackingIf(Predicate<E> pStopAttackingIfEntity, Predicate<LivingEntity> pStopAttackingWhenTarget, Consumer<E> pOnTargetErased) {
        super(pStopAttackingWhenTarget, pOnTargetErased);
        this.stopAttackingIf = pStopAttackingIfEntity;
        this.onTargetErased = pOnTargetErased;
    }

    public StopAttackingIf(Predicate<LivingEntity> pStopAttackingWhenTarget, Consumer<E> pOnTargetErased) {
        super(pStopAttackingWhenTarget, pOnTargetErased);
        this.stopAttackingIf = e -> false;
        this.onTargetErased = pOnTargetErased;
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (this.stopAttackingIf.test(pEntity)) {
            this.clearAttackTarget(pEntity);
        } else {
            super.start(pLevel, pEntity, pGameTime);
        }
    }

    @Override
    protected void clearAttackTarget(E pMemoryHolder) {
        this.onTargetErased.accept(pMemoryHolder);
        pMemoryHolder.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

        if (!pMemoryHolder.getBrain().hasMemoryValue(DragonMemoryModuleType.NEAREST_HUNTABLE)) {
            pMemoryHolder.getBrain().setActiveActivityIfPossible(Activity.IDLE);
//            pMemoryHolder.getBrain().updateActivityFromSchedule(pMemoryHolder.level.getDayTime(), pMemoryHolder.level.getGameTime());
        }
    }
}
