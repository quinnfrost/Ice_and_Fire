package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;

/**
 * Acquire a distant target, usually exceeding the 16^3 cube range of NEAREST_VISIBLE_LIVING_ENTITIES sensor.
 *
 * @param <E>
 */
@Deprecated
public class AcquireLongRangeEntitySensor<E extends Mob & IBehaviorApplicable> extends Sensor<E> {
    private Set<MemoryModuleType<?>> requiredMemory;
    /**
     * which memory to store the result
     */
    private MemoryModuleType<? extends LivingEntity> targetMemory;

    private Predicate<LivingEntity> targetPredicate;

    private boolean checkSight;

    private int range;

    public AcquireLongRangeEntitySensor(int pScanRate, Set<MemoryModuleType<?>> pRequiredMemory, MemoryModuleType<? extends LivingEntity> pTargetMemory, Predicate<LivingEntity> pPredicate, boolean pCheckSight, int pRange) {
        super(pScanRate);
        this.requiredMemory = pRequiredMemory;
        this.targetMemory = pTargetMemory;
        this.targetPredicate = pPredicate;
        this.checkSight = pCheckSight;
        this.range = pRange;
    }

    public AcquireLongRangeEntitySensor(Set<MemoryModuleType<?>> pRequiredMemory, MemoryModuleType<? extends LivingEntity> pTargetMemory, Predicate<LivingEntity> pPredicate,int pRange) {
        this(20, pRequiredMemory, pTargetMemory, pPredicate, true, pRange);
    }

    @Override
    protected void doTick(ServerLevel pLevel, E pEntity) {
        AABB aabb = pEntity.getBoundingBox().inflate(this.range);
        List<LivingEntity> list = pLevel.getEntitiesOfClass(LivingEntity.class, aabb, (livingEntity) -> {
            return livingEntity != pEntity && livingEntity.isAlive();
        });
        list.sort(Comparator.comparingDouble(pEntity::distanceToSqr));

        LivingEntity target = list.stream().filter(livingEntity -> {
            return targetPredicate.test(livingEntity) && isEntityInRangeTargetable(pEntity, livingEntity, this.range, true);
        }).findFirst().orElse(null);

        Brain<?> brain = pEntity.getBrain();
//        brain.setMemory(getTargetMemory(), Optional.ofNullable(target));

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return requiredMemory;
    }

    protected MemoryModuleType<? extends LivingEntity> getTargetMemory() {
        return targetMemory;
    }

    public static boolean isEntityInRangeTargetable(LivingEntity pAttacker, LivingEntity pTarget, double pRange, boolean checkLineOfSight) {
        return checkLineOfSight ?
                TargetingConditions.forNonCombat().range(pRange).test(pAttacker, pTarget) :
                TargetingConditions.forNonCombat().range(pRange).ignoreLineOfSight().test(pAttacker, pTarget);
    }
}
