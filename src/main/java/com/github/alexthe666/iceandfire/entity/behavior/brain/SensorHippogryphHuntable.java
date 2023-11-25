package com.github.alexthe666.iceandfire.entity.behavior.brain;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * @see net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor
 */
public class SensorHippogryphHuntable extends NearestVisibleLivingEntitySensor {
    protected int randomInterval;
    protected boolean force;

    /**
     *
     * @param randomInterval
     * @param force whether to switch target even if the current target is still valid
     */
    public SensorHippogryphHuntable(int randomInterval, boolean force) {
        this.randomInterval = randomInterval;
        this.force = force;
    }

    public SensorHippogryphHuntable() {
        this(0, true);
    }

    @Override
    protected boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget) {
//        return this.isClose(pAttacker, pTarget) && pTarget.isInWaterOrBubble() && (this.isHostileTarget(pTarget) || this.isHuntTarget(pAttacker, pTarget)) && Sensor.isEntityAttackable(pAttacker, pTarget);
        return this.isClose(pAttacker, pTarget) && (this.isHostileTarget(pTarget) || this.isHuntTarget(pAttacker, pTarget)) && LongRangeVisibleLivingEntities.isEntityInRangeAttackable(pAttacker, pTarget, 64d, true);
    }
    private boolean isHuntTarget(LivingEntity pAttacker, LivingEntity pTarget) {
        if (this.randomInterval > 0 && pAttacker.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
//        return !pAttacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && pTarget.getType().is(
//                EntityTypeTags.AXOLOTL_HUNT_TARGETS);
//        return !pAttacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
//                && pTarget.getType() == EntityType.RABBIT;
        EntityHippogryph hippogryph = (EntityHippogryph) pAttacker;
        if (hippogryph.isTame()) {
            return false;
        }
        if (pTarget != null && !pTarget.getClass().equals(hippogryph.getClass())) {
            if (hippogryph.getBbWidth() >= pTarget.getBbWidth() && hippogryph.getBbHeight() >= pTarget.getBbHeight()) {
                if (pTarget instanceof Player) {
                    return !hippogryph.isTame();
                } else {
                    if (!hippogryph.isOwnedBy(pTarget) && hippogryph.canMove() && pTarget instanceof Animal) {
                        if (hippogryph.isTame()) {
                            return DragonUtils.canTameDragonAttack(hippogryph, pTarget);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
        if (this.force || !pEntity.getBrain().hasMemoryValue(this.getMemory())) {
            pEntity.getBrain().setMemory(this.getMemory(), this.getNearestEntity(pEntity));
        }
    }

    private Optional<LivingEntity> getNearestEntity(LivingEntity pEntity) {
        return this.getVisibleEntities(pEntity).flatMap((nearestVisibleLivingEntities) -> {
            return nearestVisibleLivingEntities.findClosest((livingEntity) -> {
                return this.isMatchingEntity(pEntity, livingEntity);
            });
        });
    }

    private boolean isHostileTarget(LivingEntity pTarget) {
//        return pTarget.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
        return false;
    }

    private boolean isClose(LivingEntity pAttacker, LivingEntity pTarget) {
        return pTarget.distanceTo(pAttacker) <= 64.0D;
    }
    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return DragonMemoryModuleType.NEAREST_HUNTABLE;
    }
}
