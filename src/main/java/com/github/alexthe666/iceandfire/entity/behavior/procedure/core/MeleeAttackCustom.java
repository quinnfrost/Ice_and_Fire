package com.github.alexthe666.iceandfire.entity.behavior.procedure.core;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * @see net.minecraft.world.entity.ai.behavior.MeleeAttack
 * @param <E>
 */
@Deprecated
public class MeleeAttackCustom<E extends Mob & IFlyableBehavior> extends Behavior<E> {
    private final int cooldownBetweenAttacks;
    MemoryModuleType<LivingEntity> targetHoldingMemory;
    public MeleeAttackCustom(int pCooldownBetweenAttacks, MemoryModuleType<LivingEntity> targetHoldingMemory) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, targetHoldingMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
        this.cooldownBetweenAttacks = pCooldownBetweenAttacks;
        this.targetHoldingMemory = targetHoldingMemory;
    }

    public MeleeAttackCustom(int cooldownBetweenAttacks) {
        this(cooldownBetweenAttacks, MemoryModuleType.ATTACK_TARGET);
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        LivingEntity livingentity = this.getAttackTarget(pOwner);
        return !this.isHoldingUsableProjectileWeapon(pOwner) && BehaviorUtils.canSee(pOwner, livingentity) && BehaviorUtils.isWithinMeleeAttackRange(pOwner, livingentity);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        LivingEntity livingentity = this.getAttackTarget(pEntity);
        BehaviorUtils.lookAtEntity(pEntity, livingentity);
        pEntity.swing(InteractionHand.MAIN_HAND);
        pEntity.doHurtTarget(livingentity);
        pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
    }

    private boolean isHoldingUsableProjectileWeapon(Mob pMob) {
        return pMob.isHolding((itemStack) -> {
            Item item = itemStack.getItem();
            return item instanceof ProjectileWeaponItem && pMob.canFireProjectileWeapon((ProjectileWeaponItem)item);
        });
    }

    private LivingEntity getAttackTarget(Mob pMob) {
        return pMob.getBrain().getMemory(targetHoldingMemory).get();
    }
}
