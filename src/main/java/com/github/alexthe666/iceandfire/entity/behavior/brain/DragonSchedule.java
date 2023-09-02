package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.BehaviorRegistry;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;

public class DragonSchedule {
    public static final Schedule TEST_SCHEDULE = getScheduleBuilder().build();
    public static final Schedule HIPPOGRYPH = getScheduleBuilder()
            .changeActivityAt(10, Activity.IDLE)
            .changeActivityAt(2000, DragonActivity.HUNT)
            .changeActivityAt(12000, Activity.IDLE)
            .build();

    public static ScheduleBuilder getScheduleBuilder() {
        return new ScheduleBuilder(new Schedule());
    }
    public static Schedule makeDefaultSchedule() {
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(new Schedule());
        scheduleBuilder.changeActivityAt(0, Activity.IDLE);
        return scheduleBuilder.build();
    }
    public static void buildDeferredRegistry() {
        BehaviorRegistry.SCHEDULES.register("test_schedule", () -> TEST_SCHEDULE);
        BehaviorRegistry.SCHEDULES.register("hippogryph", () -> HIPPOGRYPH);
    }
}
