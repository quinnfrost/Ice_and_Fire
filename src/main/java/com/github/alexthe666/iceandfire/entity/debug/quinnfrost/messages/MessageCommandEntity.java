package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.EntityCommand;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.EntityCommander;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCommandEntity {
    private EntityCommand.CommandType commandType;
    private int commandEntityId;
    private Vec3 pos;
    private int targetEntityId;

    public MessageCommandEntity(EntityCommand.CommandType commandType, int commandEntityId, Vec3 pos, int targetEntityId) {
        this.commandType = commandType;
        this.commandEntityId = commandEntityId;
        this.pos = pos;
        this.targetEntityId = targetEntityId;
    }

    public MessageCommandEntity(Entity commandEntityId) {
        this(EntityCommand.CommandType.SIGNAL, commandEntityId.getId(), null, 0);
    }

    public MessageCommandEntity(Entity commandEntityId, Vec3 pos) {
        this(EntityCommand.CommandType.MOVE, commandEntityId.getId(), pos, 0);
    }

    public MessageCommandEntity(Entity commandEntityId, Entity targetEntityId) {
        this(EntityCommand.CommandType.ATTACK, commandEntityId.getId(), null, targetEntityId.getId());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(commandType.ordinal());
        buffer.writeInt(commandEntityId);
        if (commandType == EntityCommand.CommandType.MOVE) {
            buffer.writeDouble(pos.x);
            buffer.writeDouble(pos.y);
            buffer.writeDouble(pos.z);
        } else if (commandType == EntityCommand.CommandType.ATTACK) {
            buffer.writeInt(targetEntityId);
        }
    }

    public static MessageCommandEntity decoder(FriendlyByteBuf buffer) {
        EntityCommand.CommandType commandType = EntityCommand.CommandType.values()[buffer.readInt()];
        int commandEntityId = buffer.readInt();
        if (commandType == EntityCommand.CommandType.MOVE) {
            return new MessageCommandEntity(commandType, commandEntityId, new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ), 0);
        } else if (commandType == EntityCommand.CommandType.ATTACK) {
            return new MessageCommandEntity(commandType, commandEntityId, null, buffer.readInt());
        } else {
            return new MessageCommandEntity(commandType, commandEntityId, null, 0);
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Player player = Minecraft.getInstance().player;
//                if (commandType == CommandType.SIGNAL) {
                ClientGlow.setGlowing(player.level().getEntity(commandEntityId), 10);
//                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayer player = contextSupplier.get().getSender();
                ServerLevel level = player.serverLevel();
                Entity commandEntity = level.getEntity(commandEntityId);
                Entity targetEntity = level.getEntity(targetEntityId);
                if (commandType == EntityCommand.CommandType.SIGNAL) {
                    DebugUtils.getDebuggableTarget(commandEntity).ifPresent(pathfinderMob -> {
                        IceAndFire.sendMSGToPlayer(new MessageCommandEntity(EntityCommand.CommandType.SIGNAL,
                                                                            pathfinderMob.getId(),
                                                                            null,
                                                                            0
                        ), player);
                    });
                } else {
                    if (targetEntity instanceof LivingEntity livingEntity) {
                        IceAndFire.sendMSGToPlayer(new MessageCommandEntity(EntityCommand.CommandType.SIGNAL,
                                                                            livingEntity.getId(),
                                                                            null,
                                                                            livingEntity.getId()
                        ), player);
                    } else {
                        DebugUtils.getDebuggableTarget(targetEntity).ifPresent(pathfinderMob -> {
                            IceAndFire.sendMSGToPlayer(new MessageCommandEntity(EntityCommand.CommandType.SIGNAL,
                                                                                pathfinderMob.getId(),
                                                                                null,
                                                                                pathfinderMob.getId()
                            ), player);
                        });
                    }
                    EntityCommander.issueCommand(commandType,
                                                 player,
                                                 commandEntity,
                                                 pos,
                                                 targetEntity,
                                                 DebugUtils.isTracking(commandEntity)
                    );
                }
//                switch (commandType) {
//                    case SIGNAL:
//                        DebugUtils.getDebuggableTarget(level.getEntity(commandEntityId)).ifPresent(pathfinderMob -> {
//                            IceAndFire.sendMSGToPlayer(new MessageCommandEntity(EntityCommander.CommandType.SIGNAL,
//                                                                                pathfinderMob.getId(),
//                                                                                null,
//                                                                                0
//                            ), player);
//                        });
//                        break;
//                    case MOVE:
//                        DebugUtils.getDebuggableTarget(level.getEntity(commandEntityId)).ifPresent(pathfinderMob -> {
//                            pathfinderMob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0D);
//                        });
//                        break;
//                    case ATTACK:
//                        DebugUtils.getDebuggableTarget(level.getEntity(commandEntityId)).ifPresent(pathfinderMob -> {
//                            Entity target = level.getEntity(targetEntityId);
//                            if (target instanceof LivingEntity && !target.equals(pathfinderMob)) {
//                                pathfinderMob.setTarget((LivingEntity) target);
//                            } else if (target != null) {
//                                // For multipart
//                                DebugUtils.getDebuggableTarget(target).ifPresent(mob -> {
//                                    if (!mob.equals(pathfinderMob)) {
//                                        pathfinderMob.setTarget(mob);
//                                    }
//                                });
//                            }
//                        });
//                        break;
//                }
            }
        });
        return true;
    }
}
