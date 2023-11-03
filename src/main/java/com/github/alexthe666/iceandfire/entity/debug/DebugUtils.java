package com.github.alexthe666.iceandfire.entity.debug;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.gui.overlay.OverlayInfoPanel;
import com.github.alexthe666.iceandfire.client.render.pathfinding.RenderNode;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.github.alexthe666.iceandfire.message.MessageSyncPath;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DebugUtils {
    public static boolean EXTENDED_DEBUG = true;

    private static Map<Player, UUID> getTrackingMap() {
        return AbstractPathJob.trackingMap;
    }

    public static PathfinderMob getDebuggableTarget(Entity entity) {
        if (entity instanceof PathfinderMob) {
            return (PathfinderMob) entity;
        } else if (entity instanceof EntityMutlipartPart part && part.getRootParent() instanceof PathfinderMob pathfinderMob) {
            return pathfinderMob;
        } else {
            return null;
        }
    }

    public static boolean isTracking(Player player, Entity entity) {
        return getTrackingMap().getOrDefault(player, UUID.randomUUID()).equals(getDebuggableTarget(entity).getUUID());
    }

    public static boolean isTracking(Player player) {
        return getTrackingMap().containsKey(player);
    }

    public static boolean switchTracking(Player player, Entity entity) {
        if (isTracking(player, entity)) {
            stopTracking(player);
            return false;
        } else {
            startTracking(player, entity);
            return true;
        }
    }

    public static void startTracking(Player player, Entity entity) {
        // Handle debug path render
        if (EXTENDED_DEBUG) {
            PathfinderMob target = getDebuggableTarget(entity);
            if (target == null) {
                return;
            }
            getTrackingMap().put(player, target.getUUID());
            player.displayClientMessage(Component.nullToEmpty("Tracking: " + getEntityNameLong(target)), false);
        }
    }

    public static void stopTracking(Player player) {
        getTrackingMap().remove(player);
        IceAndFire.sendMSGToPlayer(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()),
                                   (ServerPlayer) player
        );
        player.displayClientMessage(Component.nullToEmpty("Stopped tracking"), false);
    }

    public static Optional<PathfinderMob> getTrackingMob(Player player) {
        if (getTrackingMap().containsKey(player)) {
            UUID uuid = getTrackingMap().get(player);
            if (player.level instanceof ServerLevel serverLevel && serverLevel.getEntity(uuid) instanceof PathfinderMob target) {
                return Optional.of(target);
            }
        }
        // Failed to get tracking mob, this might because wrong type of entity is added to track list or player is in another dimension
        // Either way, we clear the player from track list
        stopTracking(player);
        return Optional.empty();
    }

    public static String formatBlockPos(BlockPos pos) {
        if (pos != null) {
            return String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        } else {
            return "-, -, -";
        }
    }

    public static String formatVector(Vec3 vector3d) {
        if (vector3d != null) {
            return String.format("%.4f, %.4f, %.4f", vector3d.x(), vector3d.y(), vector3d.z());
        } else {
            return "-, -, -";
        }
    }

    public static double getSpeed(Mob mob) {
//        double dX = mob.getX() - mob.xOld;
//        double dY = mob.getY() - mob.yOld;
//        double dZ = mob.getZ() - mob.zOld;
        return mob.getPosition(1.0f).distanceTo(new Vec3(mob.xOld, mob.yOld, mob.zOld)) / 0.05;
    }

    public static List<String> getEntityNameLong(PathfinderMob mob) {
        return List.of(String.format("%s \"%s\" [%s] (%.1f/%s)",
                                     mob.getName().getString(),
                                     mob.getCustomName() == null ? "-" : mob.getCustomName(),
                                     mob.getEncodeId(),
                                     mob.getHealth(),
                                     Objects.toString((mob.getAttribute(
                                             Attributes.MAX_HEALTH).getValue()), "-")
                       )
        );
    }

    public static List<String> getPositionInfo(PathfinderMob mob) {
        return List.of(String.format("Pos: %.5f, %.5f, %.5f ",
                                     mob.position().x,
                                     mob.position().y,
                                     mob.position().z
                       ) + String.format("[%d, %d, %d]",
                                         mob.blockPosition().getX(),
                                         mob.blockPosition().getY(),
                                         mob.blockPosition().getZ()
                       ),
                       String.format("Rot: %.2f, %.2f ", mob.getXRot(), mob.getYRot())
        );
    }

    public static List<String> getPositionInfo(PathfinderMob mobEntity, Player player) {
        return List.of(String.format("Pos: %.5f, %.5f, %.5f ",
                                     mobEntity.position().x,
                                     mobEntity.position().y,
                                     mobEntity.position().z
                       ) + String.format("[%d, %d, %d] ",
                                         mobEntity.blockPosition().getX(),
                                         mobEntity.blockPosition().getY(),
                                         mobEntity.blockPosition().getZ()
                       ) + String.format("(%.2f)", mobEntity.distanceTo(player)),
                       String.format("Rot: %.2f, %.2f ", mobEntity.getXRot(), mobEntity.getYRot())
                               + String.format("(%s)", mobEntity.getDirection()),
                       "Motion: " + String.format("%.5f, %.5f, %.5f (%.2f)",
                                                  mobEntity.getDeltaMovement().x,
                                                  mobEntity.getDeltaMovement().y,
                                                  mobEntity.getDeltaMovement().z,
                                                  getSpeed(mobEntity)
                       ),
                       "Facing: " + String.format("%s", formatVector(mobEntity.getLookAngle()))
        );
    }

    public static void onTrackerUpdate(Player player) {
        getTrackingMob(player).ifPresent(pathfinderMob -> {
            OverlayInfoPanel.bufferInfoLeft = getTargetInfoString(pathfinderMob, player);
            RenderNode.setRenderPos(2, pathfinderMob.position().add(pathfinderMob.getDeltaMovement().scale(4f)), pathfinderMob.position(), 25555);
        });

    }

    public static List<String> getTargetInfoString(PathfinderMob mobEntity, Player player) {
        if (mobEntity == null) {
            return new ArrayList<>();
        }
        mobEntity.level.getProfiler().push("debugString");

        List<String> list = new ArrayList<>();

        list.addAll(getEntityNameLong(mobEntity));
        list.addAll(getPositionInfo(mobEntity, player));

        return list;
    }
}
