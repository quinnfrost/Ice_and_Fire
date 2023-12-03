package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class LongRangeLivingEntitySensor<E extends PathfinderMob & IBehaviorApplicable> extends NearestLivingEntitySensor {
    private final Function<E, Double> range;

    public LongRangeLivingEntitySensor(Function<E, Double> pRangeGetter) {
        super();
        this.range = pRangeGetter;
    }

    public LongRangeLivingEntitySensor(final double pRange) {
        this(livingEntity -> pRange);
    }

    public LongRangeLivingEntitySensor() {
        this(16.0D);
    }

    @Override
    protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
        AABB aabb = pEntity.getBoundingBox().inflate(this.range.apply((E) pEntity));
        List<LivingEntity> list = pLevel.getEntitiesOfClass(LivingEntity.class, aabb, (p_26717_) -> {
            return p_26717_ != pEntity && p_26717_.isAlive();
        });
        list.sort(Comparator.comparingDouble(pEntity::distanceToSqr));
        Brain<?> brain = pEntity.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, list);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new LongRangeVisibleLivingEntities(pEntity, list, this.range.apply(
                (E) pEntity)));
    }
}
