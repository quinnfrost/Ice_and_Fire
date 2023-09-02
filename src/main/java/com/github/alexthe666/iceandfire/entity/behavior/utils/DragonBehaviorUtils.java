package com.github.alexthe666.iceandfire.entity.behavior.utils;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.behavior.BehaviorHippogryph;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class DragonBehaviorUtils {
    /**
     * Get entity looking at
     * This will return the first block or entity the ray encounters, for entity ray trace this won't go through walls.
     * Other ray trace methods: EntityDragonBase#1914
     *
     * @param entity
     * @param maxDistance
     * @param excludeEntity
     * @return  A BlockRayTraceResult or EntityRayTraceResult, separated by its type
     */
    public static HitResult getTargetBlockOrEntity(Entity entity, float maxDistance, @Nullable Predicate<? super Entity> excludeEntity) {
        BlockHitResult blockRayTraceResult = getTargetBlock(entity, maxDistance, 1.0f, ClipContext.Block.COLLIDER);
        float entityRayTraceDistance = maxDistance;
        if (blockRayTraceResult.getType() != HitResult.Type.MISS) {
            entityRayTraceDistance = (float) Math.sqrt(entity.distanceToSqr(blockRayTraceResult.getLocation()));
        }
        // Limit the max ray trace distance to the first block it sees
        EntityHitResult entityRayTraceResult = getTargetEntity(entity, entityRayTraceDistance, 1.0f, null);
        return entityRayTraceResult != null ? entityRayTraceResult : blockRayTraceResult;
    }

    /**
     * Get the entity that is looking at
     * Note that this will trace through walls
     *
     * @param entity        The entity whom you want to trace its vision
     * @param maxDistance   Only entity within the distance in block is traced
     * @param partialTicks  Time in ticks to smooth the movement(linear
     *                      interpolation
     *                      or 'lerp'), use 1.0F to disable
     * @param excludeEntity Entity to exclude in tracing
     * @return Result of ray trace, or null if nothing within the distance is found
     */
    @Nullable
    public static EntityHitResult getTargetEntity(Entity entity, float maxDistance, float partialTicks,
                                                  @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) notExclude -> notExclude instanceof LivingEntity;
        }

        Vec3 vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vec3 vector3d1 = entity.getViewVector(1.0F);
        // 结束位置向量
        Vec3 vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
        float f = 1.0F;
        // 计算结束位置向量构成的区域(Bounding Box)
        AABB axisalignedbb = entity.getBoundingBox().expandTowards(vector3d1.scale(d0)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult entityraytraceresult = ProjectileUtil.getEntityHitResult(entity.level, entity, vector3d, vector3d2,
                axisalignedbb, ((Predicate<Entity>) notExclude -> !notExclude.isSpectator()
                        && notExclude.isPickable())
                        .and(excludeEntity)
        );
        return entityraytraceresult;

    }

    /**
     * Get the block that entity is looking at
     *
     * @param entity       The entity whom you want to trace its vision
     * @param maxDistance  Only blocks within the distance in block is traced
     * @param partialTicks Time in ticks to smooth the movement(linear interpolation
     *                     or 'lerp'), use 1.0F to disable
     * @param blockMode
     * @return Result of ray trace, or RayTraceResult.Type.MISS if nothing within the distance is found
     */
    public static BlockHitResult getTargetBlock(Entity entity, float maxDistance, float partialTicks, ClipContext.Block blockMode) {
        final ClipContext.Fluid fluidMode = ClipContext.Fluid.NONE;

        Vec3 vector3d = entity.getEyePosition(partialTicks);
        double d0 = maxDistance;
        double d1 = d0 * d0;

        // 获取实体视线
        Vec3 vector3d1 = entity.getViewVector(1.0F);
        // 结束位置向量
        Vec3 vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

        BlockHitResult blockRayTraceResult = entity.level.clip(
                new ClipContext(vector3d, vector3d2, blockMode, fluidMode, entity)
        );
        return blockRayTraceResult;
    }

    public static float getTargetThreatScore(Mob estimator, Entity target) {
        float threat = estimator.getBbWidth() * estimator.getBbHeight() - target.getBbWidth() * target.getBbHeight();

        if (estimator instanceof TamableAnimal tamed && tamed.isTame()) {
            LivingEntity owner = tamed.getOwner();

        }

        return threat;
    }

    public static void setAirborneState(LivingEntity livingEntity, AirborneState state) {
        if (livingEntity instanceof EntityDragonBase dragon) {
            dragon.setHovering(state == AirborneState.HOVER);
            dragon.setFlying(state == AirborneState.FLY);
        }
        if (livingEntity instanceof EntityHippogryph hippogryph) {
            hippogryph.setHovering(state == AirborneState.HOVER);
            hippogryph.setFlying(state == AirborneState.FLY);
        }
    }

    public static Optional<Direction> getCollisionDirection(Mob mob, Vec3 collisionDirection) {
        if (!mob.horizontalCollision) {
            return Optional.empty();
        }

        Direction direction = Direction.getNearest(collisionDirection.x(), collisionDirection.y(), collisionDirection.z());

        BlockPos headingPosition;
        switch (direction) {
            case NORTH -> {
                headingPosition = (new BlockPos(mob.getPosition(1.0f).x, mob.getBoundingBox().minY, mob.getBoundingBox().min(Direction.Axis.Z))).relative(Direction.Axis.Z, -1);
            }
            case SOUTH -> {
                headingPosition = (new BlockPos(mob.getPosition(1.0f).x, mob.getBoundingBox().minY, mob.getBoundingBox().max(Direction.Axis.Z))).relative(Direction.Axis.Z, 1);
            }
            case WEST -> {
                headingPosition = (new BlockPos(mob.getBoundingBox().min(Direction.Axis.X), mob.getBoundingBox().minY, mob.getPosition(1.0f).z)).relative(Direction.Axis.X, -1);
            }
            case EAST -> {
                headingPosition = (new BlockPos(mob.getBoundingBox().max(Direction.Axis.X), mob.getBoundingBox().minY, mob.getPosition(1.0f).z)).relative(Direction.Axis.X, 1);
            }
            default -> {
                return Optional.empty();
            }
        }
        for (int i = 0; i <= Mth.ceil(mob.getBbWidth() / 2.0f); i++) {
            for (int j = 0; j <= Mth.ceil(mob.getBbHeight()); j++) {
                BlockPos blockLeft = headingPosition.relative(direction.getCounterClockWise(), i).above(j);
                BlockPos blockRight = headingPosition.relative(direction.getClockWise(), i).above(j);
                if (!mob.level.isEmptyBlock(blockLeft)) {
                    return Optional.of(direction.getCounterClockWise());
                }
                if (!mob.level.isEmptyBlock(blockRight)) {
                    return Optional.of(direction.getClockWise());
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isTargetBlocked(LivingEntity livingEntity, Vec3 target) {
        if (target != null) {
            final BlockHitResult rayTrace = livingEntity.level.clip(new ClipContext(livingEntity.position().add(0, livingEntity.getEyeHeight(), 0), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity));
            final BlockPos sidePos = rayTrace.getBlockPos();
            if (!livingEntity.level.isEmptyBlock(sidePos)) {
                return true;
            } else if (!livingEntity.level.isEmptyBlock(new BlockPos(rayTrace.getLocation()))) {
                return true;
            }
            return rayTrace.getType() == HitResult.Type.BLOCK;
        }
        return false;
    }

    public static boolean isNavigatorFailedToReachTarget(Mob mob) {
        return mob.getNavigation() instanceof AdvancedPathNavigate navigator
//                && navigator.isInProgress()
                && navigator.getPath() != null
                && !navigator.getPath().canReach()
                && navigator.getPath().getDistToTarget() > 1
                && navigator.getPath().getTarget().getY() > mob.level.getMinBuildHeight()
                && mob.level.isEmptyBlock(navigator.getPath().getTarget())
                ;
    }

    public static boolean reachedTarget(Mob pMob, WalkTarget pTarget) {
        if (pMob instanceof EntityHippogryph hippogryph) {
            if (hippogryph.getAirborneState() == AirborneState.GROUNDED && hippogryph.getNavigation() instanceof AdvancedPathNavigate navigate) {
//                return navigate.lastPathFollowCompleteTimestamp == pMob.level.getGameTime() - 1;
                return navigate.lastPathFollowCompleteTimestamp == pMob.level.getGameTime() + BehaviorHippogryph.PATHFIND_TICK_TIMESTAMP_OFFSET && hasArrived(pMob, pTarget.getTarget().currentBlockPosition(), 3.0d);
            }
            return hasArrived(pMob, pTarget.getTarget().currentBlockPosition(), 2.0d);
        }
        if (pMob instanceof EntityDragonBase dragon) {

        }
        return isWithinDistManhattan(pMob, pTarget.getTarget().currentBlockPosition(), pTarget.getCloseEnoughDist());
    }

    public static boolean isWithinDistManhattan(Entity entity, BlockPos blockPos, int closeEnoughDist) {
        return blockPos.distManhattan(entity.blockPosition()) <= closeEnoughDist;
    }

    /**
     * Determine if an entity's bounding box contains the target position
     * @param entity
     * @param pos
     * @return
     */
    public static boolean hasArrived(LivingEntity entity, BlockPos pos, @Nullable Double accuracy) {
        double targetX = pos.getX();
        double targetY = pos.getY();
        double targetZ = pos.getZ();

        AABB axisAlignedBB = new AABB(targetX,targetY,targetZ,targetX,targetY,targetZ).inflate(accuracy == null ? entity.getBoundingBox().getSize() : accuracy);
//        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(targetX,targetY,targetZ,targetX,targetY,targetZ).grow(accuracy == null ? (float) Math.sqrt(2f) : accuracy);
        if (axisAlignedBB.intersects(entity.getBoundingBox())) {
            return true;
        } else {
            return false;
        }

//        return getDistance(entity.getPosition(), pos) <= Math.sqrt(8f);
    }

    public static boolean isNavigatorArrived(Mob mob, BlockPos blockPos) {
        if (mob.getNavigation() instanceof AdvancedPathNavigate navigate && navigate.getOriginalDestination() != null && navigate.getOriginalDestination().equals(blockPos) && navigate.isDone()) {
            return true;
        }
        return false;
    }

    public static boolean shouldFlyToTarget(LivingEntity livingEntity, WalkTarget walkTarget) {
        if (livingEntity instanceof EntityDragonBase dragon) {
            return dragon.isHovering() || dragon.isFlying() || shouldHoverAt(livingEntity, walkTarget);
        }
        if (livingEntity instanceof EntityHippogryph hippogryph) {
//            return hippogryph.getAirborneState() != AirborneState.GROUNDED || shouldHoverAt(livingEntity, walkTarget);
            return hippogryph.isHovering() || hippogryph.isFlying() || shouldHoverAt(livingEntity, walkTarget);
        }
        return false;
    }

    // Fixme
    public static boolean shouldHoverAt(LivingEntity dragon, WalkTarget walkTarget) {
//        if (IafDragonBehaviorHelper.isOverAir(dragon)) {
//            return true;
//        }
//        for (int i = 0; i < dragon.getDragonStage() + 1; i++) {
//            if (!dragon.level.isEmptyBlock(blockPos.offset(0, -i, 0))) {
//                return false;
//            }
//        }
//        return true;
//        return dragon.level.isEmptyBlock(new BlockPos(
//                dragon.getX(),
//                dragon.getBoundingBox().minY - 1,
//                dragon.getZ()
//                ));
        return dragon.level.isEmptyBlock(new BlockPos(
                walkTarget.getTarget().currentPosition().x(),
                walkTarget.getTarget().currentPosition().y() - 1,
                walkTarget.getTarget().currentPosition().z()
        ));

    }

    public enum AirborneState {
        GROUNDED,
        TAKEOFF,
        HOVER,
        FLY,
        LANDING
    }

}
