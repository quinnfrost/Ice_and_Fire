package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonBehaviorUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class FollowTemptationTamed extends FollowTemptation {
    private final Predicate<TamableAnimal> followPredicate;
    public Map<MemoryModuleType<?>, MemoryStatus> extraConditions;

    public FollowTemptationTamed() {
        this(tamableAnimal -> true, livingEntity -> 0.6f, ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    public FollowTemptationTamed(Predicate<TamableAnimal> followPredicate, Function<LivingEntity, Float> pSpeedModifier, Map<MemoryModuleType<?>, MemoryStatus> pExtraConditions) {
        super(pSpeedModifier);
        this.followPredicate = followPredicate;
        this.extraConditions = pExtraConditions;
    }

    private boolean checkExtraMemories(Brain<?> pBrain) {
        return this.extraConditions.entrySet().stream().allMatch((statusEntry) -> {
            MemoryModuleType<?> memorymoduletype = statusEntry.getKey();
            MemoryStatus memorystatus = statusEntry.getValue();
            return pBrain.checkMemory(memorymoduletype, memorystatus);
        });
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
        return checkExtraMemories(pOwner.getBrain()) && checkShouldFollow(pOwner) && super.checkExtraStartConditions(pLevel, pOwner);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
        return checkShouldFollow(pEntity) && super.canStillUse(pLevel, pEntity, pGameTime);
    }

    private boolean checkShouldFollow(PathfinderMob pEntity) {
        // Entry condition is not checked during running
        if (pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).isPresent()) {
            return false;
        }
        if (pEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
            return false;
//            Optional<Player> charmer = tamableAnimal.getBrain().getMemory(
//                    MemoryModuleType.TEMPTING_PLAYER);
//            return !tamableAnimal.isTame()
//                    || (charmer.isPresent() && tamableAnimal.getOwner().equals(
//                    charmer.get()));
        }
        return true;
    }

    private Optional<Player> getTemptingPlayer(PathfinderMob pPathfinder) {
        return pPathfinder.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }
}
