package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SensorTemptingTamed extends Sensor<TamableAnimal> {
    public static final int TEMPTATION_RANGE = 10;
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
    private final Ingredient temptations;

    public SensorTemptingTamed(Ingredient pTemptations) {
        this.temptations = pTemptations;
    }

    protected void doTick(ServerLevel pLevel, TamableAnimal pEntity) {
        Brain<?> brain = pEntity.getBrain();
        List<Player> list = pLevel.players().stream().filter(EntitySelector.NO_SPECTATORS).filter((player) -> {
            return TEMPT_TARGETING.test(pEntity, player) && (!pEntity.isTame() || pEntity.getOwner().equals(player));
        }).filter((player) -> {
            return pEntity.closerThan(player, 10.0D);
        }).filter(this::playerHoldingTemptation).sorted(Comparator.comparingDouble(pEntity::distanceToSqr)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            Player player = list.get(0);
            brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
        } else {
            brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }

    }

    private boolean playerHoldingTemptation(Player p_148337_) {
        return this.isTemptation(p_148337_.getMainHandItem()) || this.isTemptation(p_148337_.getOffhandItem());
    }

    private boolean isTemptation(ItemStack pStack) {
        return this.temptations.test(pStack);
    }

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }

}
