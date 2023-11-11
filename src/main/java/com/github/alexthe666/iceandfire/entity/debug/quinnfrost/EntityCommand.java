package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageCommandEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntityCommand {
    public Player issuer;
    public List<PathfinderMob> commandEntities;
    public CommandType commandType;
    public Vec3 pos;
    public List<LivingEntity> targetEntities;

    public enum CommandType {
        SIGNAL,
        MOVE,
        ATTACK
    }

    public EntityCommand() {
        this.commandEntities = new ArrayList<>();
        this.targetEntities = new ArrayList<>();
    }

    public EntityCommand(CommandType commandType, Player owner, Entity commandEntity, Vec3 pos, Entity targetEntity) {
        this.commandType = commandType;
        this.issuer = owner;
        this.pos = pos;

        this.commandEntities = new ArrayList<>();
        DebugUtils.getDebuggableTarget(commandEntity).ifPresent(pathfinderMob -> {
            this.commandEntities.add(pathfinderMob);
        });
        this.targetEntities = new ArrayList<>();
        if (targetEntity instanceof LivingEntity livingEntity) {
            this.targetEntities.add(livingEntity);
        } else {
            DebugUtils.getDebuggableTarget(targetEntity).ifPresent(pathfinderMob -> {
                this.targetEntities.add(pathfinderMob);
            });
        }
    }

    public EntityCommand set(CommandType commandType, Player owner, Entity commandEntity, Vec3 pos, Entity targetEntity) {
        return new EntityCommand(commandType, owner, commandEntity, pos, targetEntity);
    }

    public static EntityCommand empty() {
        return new EntityCommand();
    }

    public EntityCommand attack(Player owner, Entity commandEntity, Entity targetEntity) {
        return this.set(CommandType.ATTACK, owner, commandEntity, null, targetEntity);
    }

    public EntityCommand move(Player owner, Entity commandEntity, Vec3 pos) {
        return this.set(CommandType.MOVE, owner, commandEntity, pos, null);
    }


    public boolean issue() {
        if (issuer.level().isClientSide) {
            return false;
        }

        ServerPlayer player = (ServerPlayer) issuer;
        ServerLevel level = player.serverLevel();
        switch (commandType) {
            case SIGNAL:
                commandEntities.forEach(commandEntity -> {
                    IceAndFire.sendMSGToPlayer(new MessageCommandEntity(CommandType.SIGNAL,
                                                                        commandEntity.getId(),
                                                                        null,
                                                                        0
                    ), player);
                });
                return true;
            case MOVE:
                commandEntities.forEach(commandEntity -> {
                    setMoveTo(commandEntity, pos);
                });
            case ATTACK:
                commandEntities.forEach(commandEntity -> {
                    targetEntities.forEach(targetEntity -> {
                        setAttackTarget(commandEntity, targetEntity);
                    });
                });
            default:
                return false;
        }
    }

    public static void setMoveTo(PathfinderMob commandEntity, Vec3 pos) {
        if (pos != null) {
            commandEntity.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0);
            if (commandEntity instanceof EntityDragonBase dragon && (dragon.isFlying() || dragon.isHovering())) {
                dragon.flightManager.setFlightTarget(pos);
            }
        }
    }

    public static void setAttackTarget(PathfinderMob commandEntity, LivingEntity targetEntity) {
        if (!targetEntity.equals(commandEntity)) {
            commandEntity.setTarget(targetEntity);
        } else {
            commandEntity.setTarget(null);
        }
    }
}
