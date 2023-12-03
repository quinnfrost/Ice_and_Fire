package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonActivity;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSchedule;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonSensorType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BehaviorRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, IceAndFire.MODID);
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, IceAndFire.MODID);
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, IceAndFire.MODID);
    public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(ForgeRegistries.SCHEDULES, IceAndFire.MODID);
    public static void registerCustomBehaviors(IEventBus eventBus) {
        DragonMemoryModuleType.buildDeferredRegistry();
        DragonActivity.buildDeferredRegistry();
        DragonSensorType.buildDeferredRegistry();
        DragonSchedule.buildDeferredRegistry();

        BehaviorRegistry.MEMORIES.register(eventBus);
        BehaviorRegistry.ACTIVITIES.register(eventBus);
        BehaviorRegistry.SENSORS.register(eventBus);
        BehaviorRegistry.SCHEDULES.register(eventBus);
    }
}
