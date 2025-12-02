package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.EntityCommand;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.EntityCommander;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
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

    /**
     * Signal client as an ACK, usually a clients side glowing hint
     * @param commandEntity The client entity to glow
     * @return
     */
    public static MessageCommandEntity getSignalMessage(Entity commandEntity) {
        return new MessageCommandEntity(EntityCommand.CommandType.SIGNAL, commandEntity.getId(), null, 0);
    }

    /**
     * Set entity's navigation to pos, once
     * @param commandEntity Entity with navigation
     * @param pos Target position
     * @return
     */
    public static MessageCommandEntity getMoveMessage(Entity commandEntity, Vec3 pos) {
        return new MessageCommandEntity(EntityCommand.CommandType.MOVE, commandEntity.getId(), pos, 0);
    }

    /**
     * Set entity to attack targetEntity, no friendly check is performed
     * @param commandEntity Whom to attack
     * @param targetEntity Target entity to attack
     * @return
     */
    public static MessageCommandEntity getAttackMessage(Entity commandEntity, Entity targetEntity) {
        return new MessageCommandEntity(EntityCommand.CommandType.ATTACK,
                                        commandEntity.getId(),
                                        null,
                                        targetEntity.getId()
        );
    }

    /**
     * Teleport entity to pos
     * This is an op message
     * @param commandEntity
     * @param pos
     * @return
     */
    public static MessageCommandEntity getForcePosMessage(Entity commandEntity, Vec3 pos) {
        return new MessageCommandEntity(EntityCommand.CommandType.FORCE_POS, commandEntity.getId(), pos, 0);
    }

    /**
     * Stop entity's attack and clear navigation target
     * @param commandEntity
     * @return
     */
    public static MessageCommandEntity getStopMessage(Entity commandEntity) {
        return new MessageCommandEntity(EntityCommand.CommandType.STOP, commandEntity.getId(), null, 0);
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(commandType.ordinal());
        buffer.writeInt(commandEntityId);

        if (pos != null) {
            buffer.writeBoolean(true);
            buffer.writeDouble(pos.x);
            buffer.writeDouble(pos.y);
            buffer.writeDouble(pos.z);
        } else {
            buffer.writeBoolean(false);
        }
        buffer.writeInt(targetEntityId);
    }

    public static MessageCommandEntity decoder(FriendlyByteBuf buffer) {
        EntityCommand.CommandType commandType = EntityCommand.CommandType.values()[buffer.readInt()];
        int commandEntityId = buffer.readInt();
        Vec3 pos = null;
        if (buffer.readBoolean()) {
            pos = new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            );
        }
        return new MessageCommandEntity(commandType, commandEntityId, pos, buffer.readInt());

    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Player player = IceAndFire.PROXY.getClientSidePlayer();
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
                        IceAndFire.sendMSGToPlayer(MessageCommandEntity.getSignalMessage(pathfinderMob), player);
                    });
                } else {
                    if (targetEntity instanceof LivingEntity livingEntity) {
                        IceAndFire.sendMSGToPlayer(MessageCommandEntity.getSignalMessage(livingEntity), player);
                    } else {
                        DebugUtils.getDebuggableTarget(targetEntity).ifPresent(pathfinderMob -> {
                            IceAndFire.sendMSGToPlayer(MessageCommandEntity.getSignalMessage(pathfinderMob), player);
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
