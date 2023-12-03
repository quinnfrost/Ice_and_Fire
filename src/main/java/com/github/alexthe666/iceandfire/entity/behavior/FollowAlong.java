package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class FollowAlong<E extends TamableAnimal & IBehaviorApplicable> extends FlightFollowing<E> {
    public FollowAlong(int updateInterval) {
        super(ImmutableMap.of(
                DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET, MemoryStatus.VALUE_ABSENT,
                DragonMemoryModuleType.LAST_OWNER_HURT_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_ABSENT
        ), updateInterval, (entity) -> {
            return 1.2f;
        });
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return pOwner.getCommand() == 2
                && !pOwner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return pEntity.getCommand() == 2
                && !pEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected Vec3 getCenter(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (pEntity.isTame()) {
            LivingEntity owner = pEntity.getOwner();
            if (owner.level.dimension().equals(pEntity.level.dimension())) {
                return owner.position();
            }
        }
        return null;
    }

    @Override
    protected Vec3 getNextPosition(E pEntity, Vec3 center, float radius) {
        return getCenter((ServerLevel) pEntity.level, pEntity, 0);
    }
}
