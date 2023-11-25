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
public class OwnerHurtByTarget<E extends TamableAnimal & IBehaviorApplicable> extends Behavior<E> {
    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;
    public OwnerHurtByTarget() {
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
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                int i = owner.getLastHurtByMobTimestamp();
//                return i != this.timestamp && pOwner.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && pOwner.wantsToAttack(this.ownerLastHurtBy, owner);
                return owner.getLastHurtByMobTimestamp() == owner.tickCount + AI_TICK_TIMESTAMP_OFFSET && pOwner.wantsToAttack(this.ownerLastHurtBy, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, ownerLastHurtBy);
        LivingEntity livingentity = pEntity.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }

        super.start(pLevel, pEntity, pGameTime);
    }
}
