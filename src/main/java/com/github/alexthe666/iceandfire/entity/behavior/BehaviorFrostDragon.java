package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSensorType;
import com.github.alexthe666.iceandfire.entity.behavior.core.FlyAndHover;
import com.github.alexthe666.iceandfire.entity.behavior.core.LookAt;
import com.github.alexthe666.iceandfire.entity.behavior.core.WalkAndStay;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public class BehaviorFrostDragon {
    public static ImmutableList<MemoryModuleType<?>> getMemoryTypes() {
        return ImmutableList.of(
                MemoryModuleType.LOOK_TARGET,

                MemoryModuleType.WALK_TARGET,
                DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE,
                DragonMemoryModuleType.COMMAND_STAY_POSITION,
                DragonMemoryModuleType.FORBID_WALKING,
                DragonMemoryModuleType.FORBID_FLYING,
                MemoryModuleType.PATH,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.HAS_HUNTING_COOLDOWN,

                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_HOSTILE,
                DragonMemoryModuleType.NEAREST_HUNTABLE,


                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                DragonMemoryModuleType.LAST_OWNER_HURT_TARGET,
                DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET,

                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_ADULT,

                MemoryModuleType.BREED_TARGET,
                MemoryModuleType.TEMPTING_PLAYER,
                MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                MemoryModuleType.IS_TEMPTED,

                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,

                MemoryModuleType.HOME,
                DragonMemoryModuleType.FORBID_GO_HOME,

                DragonMemoryModuleType.PERSIST_MEMORY_TEST

        );
    }

    public static ImmutableList<SensorType<? extends Sensor<? super EntityDragonBase>>> getSensorTypes() {
        return ImmutableList.of(
//                SensorType.NEAREST_LIVING_ENTITIES,
                DragonSensorType.LONG_RANGE_LIVING_ENTITY_SENSOR,
                DragonSensorType.NEAREST_ADULT_TAMED,

                DragonSensorType.OWNER_HURT_BY_TARGET_SENSOR,
                DragonSensorType.OWNER_HURT_TARGET_SENSOR,
                SensorType.HURT_BY,

                DragonSensorType.SENSOR_TEST
        );
    }

    public static void registerActivities(Brain<EntityDragonBase> brain) {
        brain.addActivity(Activity.CORE, BehaviorFrostDragon.getCorePackage());
        brain.addActivity(Activity.IDLE, BehaviorFrostDragon.getIdlePackage());
    }

    public static void registerBrainGoals(Brain<EntityDragonBase> brain) {
        BehaviorFrostDragon.registerActivities(brain);
        brain.setSchedule(Schedule.EMPTY);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        brain.setSchedule(Schedule.EMPTY);
    }

    public static void updateActivity(EntityDragonBase dragon) {

    }

    public static Optional<? extends LivingEntity> findNearestValidAttackTarget(EntityDragonBase dragon) {
        return Optional.empty();
    }

    public static Ingredient getTemptations() {
        return Ingredient.EMPTY;
    }

    public static double getAwareDistance(EntityDragonBase dragon) {
        return 16d;
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityDragonBase>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(1, new Swim(0.8f)),
                Pair.of(1, new LookAt<>(45, 90)),
                Pair.of(1, new WalkAndStay<>()),
                Pair.of(1, new FlyAndHover<>()),
                Pair.of(2, new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS))
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super EntityDragonBase>>> getIdlePackage() {
        return ImmutableList.of();
    }

}
