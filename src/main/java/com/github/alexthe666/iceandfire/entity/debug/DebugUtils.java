package com.github.alexthe666.iceandfire.entity.debug;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.message.MessageSyncPath;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.UUID;

public class DebugUtils {
    public static boolean EXTENDED_DEBUG = true;
    public static boolean isTracking(Player player, Entity entity) {
        return AbstractPathJob.trackingMap.getOrDefault(player, UUID.randomUUID()).equals(entity.getUUID());
    }
    public static void startTracking(Player player, Mob mob) {
        // Handle debug path render
        if (EXTENDED_DEBUG) {
            AbstractPathJob.trackingMap.put(player, mob.getUUID());
            IceAndFire.LOGGER.debug(player.getName().getString() + " start tracking " + mob.getName().getString());
        }
    }

    public static void stopTracking(Player player) {
        AbstractPathJob.trackingMap.remove(player);
        IceAndFire.sendMSGToPlayer(new MessageSyncPath(new HashSet<>(), new HashSet<>(), new HashSet<>()),
                                   (ServerPlayer) player
        );
        IceAndFire.LOGGER.debug(player.getName().getString() + " stop tracking");
    }
}
