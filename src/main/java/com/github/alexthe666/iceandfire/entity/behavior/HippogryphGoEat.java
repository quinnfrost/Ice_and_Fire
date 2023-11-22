package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IAllMethodINeed;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class HippogryphGoEat<E extends Mob & IAllMethodINeed> extends GoToWantedItem<E> {
    public HippogryphGoEat(float pSpeedModifier, boolean pHasTarget, int pMaxDistToWalk) {
        super(pSpeedModifier, pHasTarget, pMaxDistToWalk);
    }

    public HippogryphGoEat(Predicate<E> pPredicate, float pSpeedModifier, boolean pHasTarget, int pMaxDistToWalk) {
        super(pPredicate, pSpeedModifier, pHasTarget, pMaxDistToWalk);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).isPresent() && super.checkExtraStartConditions(pLevel, pEntity);
    }

    @Override
    protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
        ItemEntity itemEntity = pOwner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
        
        if (itemEntity == null || !itemEntity.isAlive()) {
            this.stop(pLevel, pOwner, pGameTime);
        } else if (getAttackReachSqr(pOwner, itemEntity) >= pOwner.distanceToSqr(itemEntity)) {
            EntityHippogryph hippo = (EntityHippogryph) pOwner;
            itemEntity.getItem().shrink(1);
            pOwner.playSound(SoundEvents.GENERIC_EAT, 1, 1);
            hippo.setAnimation(EntityHippogryph.ANIMATION_EAT);
            hippo.feedings++;
            hippo.heal(4);
            if (hippo.feedings > 3 && (hippo.feedings > 7 || hippo.getRandom().nextInt(3) == 0) && !hippo.isTame() && itemEntity.getThrower() != null && pOwner.level.getPlayerByUUID(itemEntity.getThrower()) != null) {
                Player owner = pOwner.level.getPlayerByUUID(itemEntity.getThrower());
                if (owner != null) {
                    hippo.tame(owner);
                    hippo.setTarget(null);
                    hippo.setCommand(1);
                    hippo.setOrderedToSit(true);
                }
            }
            this.stop(pLevel, pOwner, pGameTime);
        }
    }

    protected double getAttackReachSqr(E pOwner, Entity attackTarget) {
        return pOwner.getBbWidth() * 2.0F * pOwner.getBbWidth() * 2.0F + attackTarget.getBbWidth();
    }
}
