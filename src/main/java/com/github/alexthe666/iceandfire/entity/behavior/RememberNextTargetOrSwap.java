package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IAllMethodINeed;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class RememberNextTargetOrSwap<E extends Mob & IAllMethodINeed> extends Behavior<E> {
    private final Predicate<E> canAttackPredicate;
    private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;
    private LivingEntity nextTarget;
    public RememberNextTargetOrSwap(Predicate<E> pCanAttackPredicate, Function<E, Optional<? extends LivingEntity>> pTargetFinderFunction) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED));
        this.canAttackPredicate = pCanAttackPredicate;
        this.targetFinderFunction = pTargetFinderFunction;
    }
    public RememberNextTargetOrSwap(Function<E, Optional<? extends LivingEntity>> pTargetFinderFunction) {
        this((p_24212_) -> {
            return true;
        }, pTargetFinderFunction);
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        if (pOwner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            if (!this.canAttackPredicate.test(pOwner)) {
                return false;
            } else {
                Optional<? extends LivingEntity> optional = this.targetFinderFunction.apply(pOwner);
                return optional.isPresent() ? pOwner.canAttack(optional.get()) : false;
            }
        }
        return this.nextTarget != null && this.nextTarget.isAlive();
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        Optional<? extends LivingEntity> anotherTarget = this.targetFinderFunction.apply(pEntity);
        Optional<LivingEntity> currentTarget = pEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (currentTarget.isPresent()) {
            if (anotherTarget.isPresent() && currentTarget.get() != anotherTarget.get()) {
                if (currentTarget.get().position().distanceToSqr(pEntity.getPosition(1.0f)) > 2 * pEntity.getMeleeAttackRangeSqr(currentTarget.get())
                        && anotherTarget.get().position().distanceToSqr(pEntity.getPosition(1.0f)) < 2 * pEntity.getMeleeAttackRangeSqr(anotherTarget.get())) {
                    this.nextTarget = currentTarget.get();
                    pEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, anotherTarget.get());
                } else {
                    this.nextTarget = anotherTarget.get();
                }
            }
        } else if (nextTarget != null){
            pEntity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, nextTarget);
        }
    }

    private void setAttackTarget(E pAttackTarget, LivingEntity pOwner) {
        net.minecraftforge.event.entity.living.LivingChangeTargetEvent changeTargetEvent = net.minecraftforge.common.ForgeHooks.onLivingChangeTarget(pAttackTarget, pOwner, net.minecraftforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
        if(!changeTargetEvent.isCanceled()) {
            pAttackTarget.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, changeTargetEvent.getNewTarget());
            pAttackTarget.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            net.minecraftforge.common.ForgeHooks.onLivingSetAttackTarget(pAttackTarget, changeTargetEvent.getNewTarget(), net.minecraftforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET); // TODO: Remove in 1.20
        }
    }
}
