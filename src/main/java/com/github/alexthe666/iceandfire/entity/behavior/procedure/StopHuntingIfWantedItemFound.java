package com.github.alexthe666.iceandfire.entity.behavior.procedure;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IFlyableBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopHuntingIfWantedItemFound<E extends Mob & IFlyableBehavior> extends StopAttackingIfTargetInvalid<E> {
    public StopHuntingIfWantedItemFound() {
        super(livingEntity -> {
                  return true;
              },
              e -> {
                  e.getBrain().eraseMemory(DragonMemoryModuleType.NEAREST_HUNTABLE);
                  e.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
              }
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return pOwner.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (pEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
            this.clearAttackTarget(pEntity);
        } else {
            super.start(pLevel, pEntity, pGameTime);
        }
    }
}
