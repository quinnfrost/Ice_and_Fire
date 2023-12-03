package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@Deprecated
public class OwnerHurtTarget<E extends TamableAnimal & IBehaviorApplicable> extends Behavior<E> {
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    private LivingEntity ownerLastHurt;
    private int timestamp;
    public OwnerHurtTarget() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        if (pOwner.isTame() /* && !pOwner.isOrderedToSit() */) {
            LivingEntity owner = pOwner.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.ownerLastHurt = owner.getLastHurtMob();
                int i = owner.getLastHurtMobTimestamp();
//                return i != this.timestamp && pOwner.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && pOwner.wantsToAttack(this.ownerLastHurt, owner);
                return owner.getLastHurtMobTimestamp() == owner.tickCount + AI_TICK_TIMESTAMP_OFFSET && pOwner.wantsToAttack(this.ownerLastHurt, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, ownerLastHurt);
        LivingEntity livingentity = pEntity.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start(pLevel, pEntity, pGameTime);
    }
}
