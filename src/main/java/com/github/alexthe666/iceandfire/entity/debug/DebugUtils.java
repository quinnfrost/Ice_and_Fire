package com.github.alexthe666.iceandfire.entity.debug;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.github.alexthe666.iceandfire.message.MessageSyncPath;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class DebugUtils {
    public static boolean EXTENDED_DEBUG = true;
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
        return AbstractPathJob.trackingMap.getOrDefault(player, UUID.randomUUID()).equals(getDebuggableTarget(entity).getUUID());
    }
    public static boolean isTracking(Player player) {
        return AbstractPathJob.trackingMap.containsKey(player);
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
            AbstractPathJob.trackingMap.put(player, target.getUUID());
            IceAndFire.LOGGER.debug(player.getName().getString() + " start tracking " + target.getName().getString());
            player.displayClientMessage(Component.nullToEmpty("Tracking " + target.getName().getString()), false);
        }
    }

    public static void stopTracking(Player player) {
        AbstractPathJob.trackingMap.remove(player);
        IceAndFire.sendMSGToPlayer(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()),
                                   (ServerPlayer) player
        );
        IceAndFire.LOGGER.debug(player.getName().getString() + " stop tracking");
        player.displayClientMessage(Component.nullToEmpty("Stop tracking"), false);
    }

    public static Optional<PathfinderMob> getTrackingMob(Player player) {
        if (AbstractPathJob.trackingMap.containsKey(player)) {
            UUID uuid = AbstractPathJob.trackingMap.get(player);
            if (player.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(uuid) instanceof PathfinderMob target) {
                return Optional.of(target);
            }
        }
        // Failed to get tracking mob, this might because wrong type of entity is added to track list or player is in another dimension
        // Either way, we clear the player from track list
        stopTracking(player);
        return Optional.empty();
    }

    public static void onTrackerUpdate(Player player) {
        if (getTrackingMob(player).isPresent()) {
            player.displayClientMessage(Component.nullToEmpty("Mob position: " + getTrackingMob(player).get().blockPosition().toString()), true);
        }

    }
}
