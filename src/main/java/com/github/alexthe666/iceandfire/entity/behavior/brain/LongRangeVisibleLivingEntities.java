package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LongRangeVisibleLivingEntities extends NearestVisibleLivingEntities {
    private final List<LivingEntity> nearbyEntities;
    private final Predicate<LivingEntity> lineOfSightTest;
    public LongRangeVisibleLivingEntities(LivingEntity pLivingEntity, List<LivingEntity> pNearbyLivingEntities, double pRange) {
        super(pLivingEntity, pNearbyLivingEntities);
        this.nearbyEntities = pNearbyLivingEntities;
        Object2BooleanOpenHashMap<LivingEntity> object2booleanopenhashmap = new Object2BooleanOpenHashMap<>(pNearbyLivingEntities.size());
        Predicate<LivingEntity> predicate = (livingEntity) -> {
            return isEntityInRangeTargetable(pLivingEntity, livingEntity, pRange, true);
        };
        this.lineOfSightTest = (livingEntity) -> {
            return object2booleanopenhashmap.computeBooleanIfAbsent(livingEntity, predicate);
        };
    }

    public LongRangeVisibleLivingEntities(LivingEntity pLivingEntity, List<LivingEntity> pNearbyLivingEntities) {
        this(pLivingEntity, pNearbyLivingEntities, 16.0D);
    }

    /**
     *
     * @param pAttacker
     * @param pTarget
     * @param pRange
     * @param checkLineOfSight Check if there's obstacle between the attacker and the target, attacker's look direction does not matter
     * @return
     */
    public static boolean isEntityInRangeTargetable(LivingEntity pAttacker, LivingEntity pTarget, double pRange, boolean checkLineOfSight) {
        TargetingConditions targetingconditions = TargetingConditions.forNonCombat().range(pRange);
        if (!checkLineOfSight) {
            targetingconditions = targetingconditions.ignoreLineOfSight();
        }
        if (pAttacker.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget)) {
            targetingconditions = targetingconditions.ignoreInvisibilityTesting();
        }
        return targetingconditions.test(pAttacker, pTarget);
    }

    public static boolean isEntityInRangeAttackable(LivingEntity pAttacker, LivingEntity pTarget, double pRange, boolean checkLineOfSight) {
        TargetingConditions targetingconditions = TargetingConditions.forCombat().range(pRange);
        if (!checkLineOfSight) {
            targetingconditions = targetingconditions.ignoreLineOfSight();
        }
        if (pAttacker.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget)) {
            targetingconditions = targetingconditions.ignoreInvisibilityTesting();
        }
        return targetingconditions.test(pAttacker, pTarget);
    }

    // Below is not touched
    public Optional<LivingEntity> findClosest(Predicate<LivingEntity> pPredicate) {
        for(LivingEntity livingentity : this.nearbyEntities) {
            if (pPredicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
                return Optional.of(livingentity);
            }
        }

        return Optional.empty();
    }

    public Iterable<LivingEntity> findAll(Predicate<LivingEntity> pPredicate) {
        return Iterables.filter(this.nearbyEntities, (p_186127_) -> {
            return pPredicate.test(p_186127_) && this.lineOfSightTest.test(p_186127_);
        });
    }

    public Stream<LivingEntity> find(Predicate<LivingEntity> pPredicate) {
        return this.nearbyEntities.stream().filter((p_186120_) -> {
            return pPredicate.test(p_186120_) && this.lineOfSightTest.test(p_186120_);
        });
    }

    public boolean contains(LivingEntity pEntity) {
        return this.nearbyEntities.contains(pEntity) && this.lineOfSightTest.test(pEntity);
    }

    public boolean contains(Predicate<LivingEntity> pPredicate) {
        for(LivingEntity livingentity : this.nearbyEntities) {
            if (pPredicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
                return true;
            }
        }

        return false;
    }
}
