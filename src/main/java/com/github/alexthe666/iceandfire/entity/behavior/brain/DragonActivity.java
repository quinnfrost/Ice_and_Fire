package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.BehaviorRegistry;
import net.minecraft.world.entity.schedule.Activity;

public class DragonActivity {
    public static final Activity STAY = new Activity("stay");
    public static final Activity GUARD = new Activity("guard");
    public static final Activity FOLLOW = new Activity("follow");
    public static final Activity SIT = new Activity("sit");
    public static final Activity HUNT = new Activity("hunt");

    // Fixme: namespace???
    public static void buildDeferredRegistry() {
        BehaviorRegistry.ACTIVITIES.register(STAY.getName(), () -> STAY);
        BehaviorRegistry.ACTIVITIES.register(GUARD.getName(), () -> GUARD);
        BehaviorRegistry.ACTIVITIES.register(FOLLOW.getName(), () -> FOLLOW);

        BehaviorRegistry.ACTIVITIES.register(SIT.getName(), () -> SIT);
        BehaviorRegistry.ACTIVITIES.register(HUNT.getName(), () -> HUNT);
    }
}
