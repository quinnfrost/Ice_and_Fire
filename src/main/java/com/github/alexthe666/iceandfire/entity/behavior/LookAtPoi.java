package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class LookAtPoi<E extends Mob & IBehaviorApplicable> extends Behavior<E> {
    private final Function<E, Optional<Vec3>> posProvider;
    private Optional<Vec3> targetPosTest = Optional.empty();

    public LookAtPoi(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pMinDuration, int pMaxDuration, Function<E, Optional<Vec3>> posProvider) {
        super(pEntryCondition, pMinDuration, pMaxDuration);
        this.posProvider = posProvider;
    }

    public LookAtPoi(Function<E, Optional<Vec3>> posProvider, int pMinDuration, int pMaxDuration) {
        this(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT),
             pMinDuration,
             pMaxDuration,
             posProvider
        );
    }

    public LookAtPoi(int pMinDuration, int pMaxDuration) {
        this(e -> {
            double d0 = (Math.PI * 2D) * e.getRandom().nextDouble();
            double relX = Math.cos(d0);
            double relZ = Math.sin(d0);
            return Optional.of(new Vec3(e.getX() + relX, e.getEyeY(), e.getZ() + relZ));
        }, pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        this.targetPosTest = this.posProvider.apply(pOwner);
        return this.targetPosTest.isPresent();
    }

    @Override
    protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
        pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,
                                     new BlockPosTracker(new BlockPos(this.targetPosTest.get()))
        );
        this.targetPosTest = Optional.empty();
    }
}
