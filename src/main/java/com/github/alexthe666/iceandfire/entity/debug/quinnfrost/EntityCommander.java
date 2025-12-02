package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityCommander {
    public static Map<Player, List<EntityCommand>> activeCommands = new HashMap<>();

    public static void onTrackerUpdate(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        activeCommands.forEach((player1, entityCommands) -> {
            entityCommands.forEach(EntityCommand::issue);
        });
    }

    public static void addCommand(Player player, EntityCommand command) {
        player.displayClientMessage(Component.nullToEmpty(String.format("%s issued command: %s", player.getName().getString(), command)), false);

        activeCommands.put(player, List.of(command));
    }

    public static void removeCommand(Player player) {
        activeCommands.remove(player);
    }

    /**
     * Build custom command type
     * Arguments have different meanings based on command type
     * No argument check is performed on the other side
     *
     * @param commandType Operation to perform
     * @param owner Player who issued the command
     * @param commandEntity Entity1, usually entity owned by commander
     * @param pos Pos1, usually target position
     * @param targetEntity Entity2, usually the other entity involved
     * @param force Whether command will be issued repeatedly
     * @return
     */
    public static boolean issueCommand(EntityCommand.CommandType commandType, Player owner, Entity commandEntity, Vec3 pos, Entity targetEntity, boolean force) {
        if (owner.level().isClientSide) {
            return false;
        }

        // 使用流式接口创建和配置 EntityCommand 实例
        EntityCommand command = new EntityCommand(commandType, owner)
                .withCommandEntity(commandEntity)
                .withTargetEntity(targetEntity)
                .atPosition(pos);

        if (force) {
            // 如果是持续性命令，添加到 activeCommands
            addCommand(owner, command);
            return true;
        }

        // 如果是一次性命令，立即执行
        return command.issue();
    }

}
