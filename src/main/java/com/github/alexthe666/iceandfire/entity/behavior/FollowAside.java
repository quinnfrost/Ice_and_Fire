package com.github.alexthe666.iceandfire.entity.behavior;

import com.github.alexthe666.iceandfire.entity.behavior.brain.DragonMemoryModuleType;
import com.github.alexthe666.iceandfire.entity.behavior.utils.DragonFlightUtils;
import com.github.alexthe666.iceandfire.entity.behavior.utils.IBehaviorApplicable;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class FollowAside<E extends TamableAnimal & IBehaviorApplicable> extends FlightFollowing<E>{
    public FollowAside(int updateInterval) {
        super(ImmutableMap.of(
                DragonMemoryModuleType.LAST_OWNER_HURT_BY_TARGET, MemoryStatus.VALUE_ABSENT,
                DragonMemoryModuleType.LAST_OWNER_HURT_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_ABSENT
        ), updateInterval);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
        return pOwner.getCommand() == 2
                && !pOwner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
        return pEntity.getCommand() == 2
                && !pEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
        this.lastCenter = null;

        pEntity.getBrain().eraseMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE);
    }

    @Override
    protected Vec3 getCenter(ServerLevel pLevel, E pEntity, long pGameTime) {
        if (pEntity.isTame()) {
            LivingEntity owner = pEntity.getOwner();
            if (owner.level.dimension().equals(pEntity.level.dimension())) {
                return owner.position();
            }
        }
        return null;
    }

    protected long stillTick = 0;
    protected long airborneTick = 0;
    protected Vec3 lastCenter;

    @Override
    protected Vec3 getNextPosition(E pEntity, Vec3 escortCenter, float radius) {
        Vec3 center = escortCenter;

        if (this.lastCenter != null) {
            if (this.lastCenter.distanceTo(center) > 0.1) {
                this.stillTick = 0;
            } else {
                this.stillTick++;
            }

            if (!DragonFlightUtils.isBlockPassable(pEntity.level, Heightmap.Types.MOTION_BLOCKING, (new BlockPos(center)).below())) {
                this.airborneTick = 0;
            } else {
                this.airborneTick++;
            }

            if (this.airborneTick < 20) {
                center = DragonFlightUtils.getGround(pEntity.level, center);
                pEntity.getBrain().setMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, DragonMemoryModuleType.NavigationType.WALK);
            } else {
//                pEntity.getBrain().eraseMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE);
                pEntity.getBrain().setMemory(DragonMemoryModuleType.PREFERRED_NAVIGATION_TYPE, DragonMemoryModuleType.NavigationType.ANY);
            }

            Vec3 direction = center.subtract(this.lastCenter).normalize();
            if (this.stillTick < 20) {
                if (direction.horizontalDistanceSqr() > 1.0E-06) {

                    Pair<Vec3, Vec3> wantedPos = FollowPosition.GROUND.applyShift(center, direction);
                    this.lastCenter = center;
                    return pEntity.distanceToSqr(wantedPos.getFirst()) > pEntity.distanceToSqr(wantedPos.getSecond())
                            ? wantedPos.getSecond()
                            : wantedPos.getFirst();
                } else {
                    this.lastCenter = center;
                    return null;
                }
            }
        }

        this.lastCenter = center;
        return center.add(pEntity.position().subtract(center).normalize().scale(radius));
    }

    public static enum FollowPosition {
        GROUND {
            @Override
            public <E extends LivingEntity> Pair<Vec3, Vec3> applyShift(Vec3 center, Vec3 direction) {
                return Pair.of(center.add(
                                       direction.yRot((float) Math.PI / 2.0f).scale(4)
                                               .add(direction.scale(8))
                               ), center.add(
                                       direction.yRot(-(float) Math.PI / 2.0f).scale(4)
                                               .add(direction.scale(8))
                               )
                );
            }
        },
        AIR {
            @Override
            public <E extends LivingEntity> Pair<Vec3, Vec3> applyShift(Vec3 center, Vec3 direction) {
                return Pair.of(center.add(
                                       direction.yRot((float) Math.PI / 2.0f).scale(4)
                                               .add(direction.scale(8))
                                               .add(0, 3, 0)
                               ), center.add(
                                       direction.yRot(-(float) Math.PI / 2.0f).scale(4)
                                               .add(direction.scale(8))
                                               .add(0, 3, 0)
                               )
                );
            }
        };

        public abstract <E extends LivingEntity> Pair<Vec3, Vec3> applyShift(Vec3 center, Vec3 direction);
    }

}
