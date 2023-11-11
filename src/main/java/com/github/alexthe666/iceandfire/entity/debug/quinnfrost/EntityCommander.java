package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class EntityCommander {
    public static Map<Player, EntityCommand> activeCommands = new HashMap<>();

    public static void onTrackerUpdate(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        activeCommands.forEach((player1, entityCommand) -> {
            entityCommand.issue();
        });
    }

    public static void addCommand(Player player, EntityCommand command) {
        activeCommands.put(player, command);
    }

    public static void removeCommand(Player player) {
        activeCommands.remove(player);
    }

    public static boolean issueCommand(EntityCommand.CommandType commandType, Player owner, Entity commandEntity, Vec3 pos, Entity targetEntity, boolean force) {
        if (owner.level().isClientSide) {
            return false;
        }

        ServerPlayer player = (ServerPlayer) owner;
        ServerLevel level = player.serverLevel();

        if (force) {
            addCommand(player, new EntityCommand(commandType, owner, commandEntity, pos, targetEntity));
            return true;
        }
        return (new EntityCommand()).set(commandType, owner, commandEntity, pos, targetEntity).issue();
    }

}
