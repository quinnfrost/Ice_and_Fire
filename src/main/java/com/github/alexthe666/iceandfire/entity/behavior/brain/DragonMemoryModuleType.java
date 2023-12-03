package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.behavior.BehaviorRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.UUID;

public class DragonMemoryModuleType {
    public static final MemoryModuleType<String> PERSIST_MEMORY_TEST = new MemoryModuleType<>(Optional.of(Codec.STRING));
    public static final MemoryModuleType<String> VOLATILE_MEMORY_TEST = new MemoryModuleType<>(Optional.empty());

//    public static final MemoryModuleType<Integer> COMMAND = new MemoryModuleType<>(Optional.of(Codec.INT));
    public static final MemoryModuleType<GlobalPos> COMMAND_STAY_POSITION = new MemoryModuleType<>(Optional.of(GlobalPos.CODEC));
    public static final MemoryModuleType<UUID> COMMAND_ATTACK_TARGET = new MemoryModuleType<>(Optional.of(
            SerializableUUID.CODEC));
    public static final MemoryModuleType<Boolean> FORBID_WALKING = new MemoryModuleType<>(Optional.of(Codec.BOOL));
    public static final MemoryModuleType<Boolean> FORBID_FLYING = new MemoryModuleType<>(Optional.of(Codec.BOOL));
    public static final MemoryModuleType<Boolean> FORBID_GO_HOME = new MemoryModuleType<>(Optional.of(Codec.BOOL));

    public static final MemoryModuleType<NavigationType> PREFERRED_NAVIGATION_TYPE = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> LAST_OWNER_HURT_TARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> LAST_OWNER_HURT_BY_TARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> NEAREST_HUNTABLE = new MemoryModuleType<>(Optional.empty());

    public enum NavigationType {
        ANY,
        WALK,
        FLY
    }

    public static void buildDeferredRegistry() {
        BehaviorRegistry.MEMORIES.register("persist_mem_test", () -> PERSIST_MEMORY_TEST);
        BehaviorRegistry.MEMORIES.register("volatile_mem_test", () -> VOLATILE_MEMORY_TEST);

//        BehaviorRegistry.MEMORIES.register("command", () -> COMMAND);
        BehaviorRegistry.MEMORIES.register("command_stay_position", () -> COMMAND_STAY_POSITION);
        BehaviorRegistry.MEMORIES.register("command_attack_target", () -> COMMAND_ATTACK_TARGET);
        BehaviorRegistry.MEMORIES.register("forbid_walking", () -> FORBID_WALKING);
        BehaviorRegistry.MEMORIES.register("forbid_flying", () -> FORBID_FLYING);
        BehaviorRegistry.MEMORIES.register("forbid_go_home", () -> FORBID_GO_HOME);


        BehaviorRegistry.MEMORIES.register("preferred_navigation_type", () -> PREFERRED_NAVIGATION_TYPE);
        BehaviorRegistry.MEMORIES.register("last_owner_hurt_target", () -> LAST_OWNER_HURT_TARGET);
        BehaviorRegistry.MEMORIES.register("last_owner_hurt_by_target", () -> LAST_OWNER_HURT_BY_TARGET);
        BehaviorRegistry.MEMORIES.register("nearest_huntable", () -> NEAREST_HUNTABLE);

    }

}
