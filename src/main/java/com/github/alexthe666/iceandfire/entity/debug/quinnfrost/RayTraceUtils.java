package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class RayTraceUtils {
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
        EntityHitResult entityRayTraceResult = getTargetEntity(entity, entityRayTraceDistance, 1.0f, excludeEntity);
        if (entityRayTraceResult != null) {
            return entityRayTraceResult;
        } else {
            return blockRayTraceResult;
        }
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

    /**
     * Find out if the target's owner nbt tag matches the input owner's UUID
     * If any of the input is null, return false
     *
     * @param target The entity's owner to determine
     * @param owner  Owner to determine
     * @return True if two matches, False if two do not match or cannot be owned
     */
    public static boolean isOwner(@Nullable LivingEntity target, @Nullable LivingEntity owner) {
        if (target == null || owner == null) {
            return false;
        }
        try {
            CompoundTag compoundNBT = new CompoundTag();
            target.addAdditionalSaveData(compoundNBT);
            if (compoundNBT.getUUID("Owner").equals(owner.getUUID())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }
}
