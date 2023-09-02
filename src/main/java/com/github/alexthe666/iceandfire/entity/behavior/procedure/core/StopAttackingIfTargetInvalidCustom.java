package com.github.alexthe666.iceandfire.entity.behavior.procedure.core;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Deprecated
public class StopAttackingIfTargetInvalidCustom<E extends Mob & IFlyableBehavior> extends Behavior<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
    private final Predicate<LivingEntity> stopAttackingWhen;
    private final Consumer<E> onTargetErased;
    private final MemoryModuleType<LivingEntity> targetHoldingMemory;
    public StopAttackingIfTargetInvalidCustom(Predicate<LivingEntity> pStopAttackingWhen, Consumer<E> pOnTargetErased, MemoryModuleType<LivingEntity> targetHoldingMemory) {
        super(ImmutableMap.of(targetHoldingMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
        this.stopAttackingWhen = pStopAttackingWhen;
        this.onTargetErased = pOnTargetErased;
        this.targetHoldingMemory = targetHoldingMemory;
    }

    public StopAttackingIfTargetInvalidCustom(Predicate<LivingEntity> pStopAttackingWhen) {
        this(pStopAttackingWhen, (e) -> {
        }, MemoryModuleType.ATTACK_TARGET);
    }

    public StopAttackingIfTargetInvalidCustom(Consumer<E> pOnTargetErased) {
        this((livingEntity) -> {
            return false;
        }, pOnTargetErased, MemoryModuleType.ATTACK_TARGET);
    }

    public StopAttackingIfTargetInvalidCustom() {
        this((livingEntity) -> {
            return false;
        }, (e) -> {
        }, MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        LivingEntity livingentity = this.getAttackTarget(pEntity);
        if (!pEntity.canAttack(livingentity)) {
            this.clearAttackTarget(pEntity);
        } else if (isTiredOfTryingToReachTarget(pEntity)) {
            this.clearAttackTarget(pEntity);
        } else if (this.isCurrentTargetDeadOrRemoved(pEntity)) {
            this.clearAttackTarget(pEntity);
        } else if (this.isCurrentTargetInDifferentLevel(pEntity)) {
            this.clearAttackTarget(pEntity);
        } else if (this.stopAttackingWhen.test(this.getAttackTarget(pEntity))) {
            this.clearAttackTarget(pEntity);
        }
    }

    protected boolean isCurrentTargetInDifferentLevel(E pMemoryHolder) {
        return this.getAttackTarget(pMemoryHolder).level != pMemoryHolder.level;
    }

    protected LivingEntity getAttackTarget(E pMemoryHolder) {
        return pMemoryHolder.getBrain().getMemory(targetHoldingMemory).get();
    }

    protected static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E pMemoryHolder) {
        Optional<Long> optional = pMemoryHolder.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return optional.isPresent() && pMemoryHolder.level.getGameTime() - optional.get() > 200L;
    }

    protected boolean isCurrentTargetDeadOrRemoved(E pMemoryHolder) {
        Optional<LivingEntity> optional = pMemoryHolder.getBrain().getMemory(targetHoldingMemory);
        return optional.isPresent() && !optional.get().isAlive();
    }

    protected void clearAttackTarget(E pMemoryHolder) {
        this.onTargetErased.accept(pMemoryHolder);
        pMemoryHolder.getBrain().eraseMemory(targetHoldingMemory);
    }
}
