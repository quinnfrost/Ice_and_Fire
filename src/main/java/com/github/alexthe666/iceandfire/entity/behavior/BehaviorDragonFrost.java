package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonActivity;
import com.github.alexthe666.iceandfire.entity.behavior.procedure.core.FlyAndHover;
import com.github.alexthe666.iceandfire.entity.behavior.procedure.core.LookAt;
import com.github.alexthe666.iceandfire.entity.behavior.procedure.core.WalkAndStay;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class BehaviorDragonFrost {
    public static void registerActivities(Brain<EntityDragonBase> brain) {
        brain.addActivity(Activity.CORE, BehaviorDragonFrost.getCorePackage());
        brain.addActivity(Activity.IDLE, BehaviorDragonFrost.getIdlePackage());
    }

    public static void updateActivity(EntityDragonBase dragon) {

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
        return ImmutableList.of(
                Pair.of(1, new DoNothing(20, 60))
        );
    }
}
