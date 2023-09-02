package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.BehaviorHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.BehaviorRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.Optional;

public class DragonSensorType {
    public static final SensorType<SensorDragonTest> SENSOR_TEST = new SensorType<>(SensorDragonTest::new);

    public static final SensorType<SensorNearestLivingEntityCustom<LivingEntity>> NEARBY_LIVING_ENTITIES = new SensorType<>(SensorNearestLivingEntityCustom::new);

    public static final SensorType<SensorOwnerHurtTarget> OWNER_HURT_TARGET_SENSOR = new SensorType<>(() -> new SensorOwnerHurtTarget());
    public static final SensorType<SensorOwnerHurtByTarget> OWNER_HURT_BY_TARGET_SENSOR = new SensorType<>(() -> new SensorOwnerHurtByTarget());
    public static final SensorType<SensorNearestAdultTamed> NEAREST_ADULT_TAMED = new SensorType<>(
            SensorNearestAdultTamed::new);
    public static final SensorType<SensorNearestItemTamed> NEAREST_WANTED_ITEM_TAMED = new SensorType<>(
            SensorNearestItemTamed::new);
    public static final SensorType<SensorTemptingTamed> HIPPOGRYPH_TEMPTATIONS = new SensorType<>(() -> new SensorTemptingTamed(BehaviorHippogryph.getTemptations()));
    public static final SensorType<SensorHippogryphHuntable> HIPPOGRYPH_HUNTABLES = new SensorType<>(() -> new SensorHippogryphHuntable());
    public static void buildDeferredRegistry() {
        BehaviorRegistry.SENSORS.register("sensor_test", () -> SENSOR_TEST);
        BehaviorRegistry.SENSORS.register("nearby_living_entities", () -> NEARBY_LIVING_ENTITIES);

        BehaviorRegistry.SENSORS.register("owner_hurt_target_sensor", () -> OWNER_HURT_TARGET_SENSOR);
        BehaviorRegistry.SENSORS.register("owner_hurt_by_target_sensor", () -> OWNER_HURT_BY_TARGET_SENSOR);

        BehaviorRegistry.SENSORS.register("nearest_adult_tamed", () -> NEAREST_ADULT_TAMED);
        BehaviorRegistry.SENSORS.register("nearest_wanted_item_tamed", () -> NEAREST_WANTED_ITEM_TAMED);
        BehaviorRegistry.SENSORS.register("hippogryph_temptations", () -> HIPPOGRYPH_TEMPTATIONS);
        BehaviorRegistry.SENSORS.register("hippogryph_huntables", () -> HIPPOGRYPH_HUNTABLES);

    }

    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();

    public static final int AI_TICK_TIMESTAMP_OFFSET = -1;
    public static Optional<LivingEntity> getAssistOwnerTarget(TamableAnimal tamable) {
        if (tamable.isTame()) {
            LivingEntity owner = tamable.getOwner();
            LivingEntity ownerLastHurtBy = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
//                return i != this.timestamp && pOwner.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && pOwner.wantsToAttack(this.ownerLastHurtBy, owner);
            if (ownerLastHurtBy != null && owner.getLastHurtByMobTimestamp() == owner.tickCount + AI_TICK_TIMESTAMP_OFFSET && tamable.wantsToAttack(ownerLastHurtBy, owner)) {
                return Optional.of(ownerLastHurtBy);
            }

            LivingEntity ownerLastHurt = owner.getLastHurtMob();
            i = owner.getLastHurtMobTimestamp();
//                return i != this.timestamp && pOwner.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && pOwner.wantsToAttack(this.ownerLastHurt, owner);
            if (ownerLastHurt != null && owner.getLastHurtMobTimestamp() == owner.tickCount + AI_TICK_TIMESTAMP_OFFSET && tamable.wantsToAttack(ownerLastHurt, owner)) {
                return Optional.of(ownerLastHurt);
            }

        }
        return Optional.empty();
    }
}
