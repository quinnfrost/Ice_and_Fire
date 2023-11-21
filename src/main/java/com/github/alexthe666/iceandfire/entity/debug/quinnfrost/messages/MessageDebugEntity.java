package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.ExtendedEntityDebugger;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay.OverlayInfoPanel;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.RenderNode;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageDebugEntity {
    private final boolean isActive;
    private int entityId;
    private List<Vec3> associatedTarget;
    private List<String> serverEntityInfo;

    public MessageDebugEntity(int entityIdIn, List<Vec3> associatedTargetIn, List<String> serverEntityInfoIn) {
        this.isActive = true;
        this.entityId = entityIdIn;
        this.associatedTarget = associatedTargetIn;
        this.serverEntityInfo = serverEntityInfoIn;
    }

    public MessageDebugEntity() {
        this.isActive = false;
    }

    public MessageDebugEntity(int entityIdIn) {
        this(entityIdIn, new ArrayList<>(), new ArrayList<>());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isActive);
        if (isActive) {
            buffer.writeInt(entityId);

            buffer.writeInt(associatedTarget.size());
            for (Vec3 target :
                    associatedTarget) {
                buffer.writeDouble(target.x);
                buffer.writeDouble(target.y);
                buffer.writeDouble(target.z);
            }

            buffer.writeInt(serverEntityInfo.size());
            for (String infoItem :
                    serverEntityInfo) {
                buffer.writeUtf(infoItem);
            }
        }
    }

    public static MessageDebugEntity decoder(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            int entityId = buffer.readInt();
            int length = buffer.readInt();
            List<Vec3> associatedTarget = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                associatedTarget.add(new Vec3(
                        buffer.readDouble(),
                        buffer.readDouble(),
                        buffer.readDouble()
                ));
            }

            length = buffer.readInt();
            List<String> serverEntityInfo = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                serverEntityInfo.add(buffer.readUtf());
            }

            return new MessageDebugEntity(entityId, associatedTarget, serverEntityInfo);
        } else {
            return new MessageDebugEntity();
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                if (isActive) {
                    Entity entity = IceAndFire.PROXY.getClientSidePlayer().level().getEntity(entityId);
                    if (!(entity instanceof PathfinderMob)) {
                        return;
                    }
                    PathfinderMob targetEntity = (PathfinderMob) entity;

                    if (serverEntityInfo.size() != 0) {

                        for (Vec3 target :
                                associatedTarget) {
                            RenderNode.drawCube(2, target, false, null);
                            RenderNode.drawLine(2, targetEntity.position(), target, null);
                        }

                        OverlayInfoPanel.bufferInfoLeft = serverEntityInfo;
                        OverlayInfoPanel.bufferInfoRight = DebugUtils.getTargetInfoString(targetEntity,
                                                                                          IceAndFire.PROXY.getClientSidePlayer()
                        );
                    } else {
                        ClientGlow.setGlowing(targetEntity, 10);
                    }
                } else {
                    OverlayInfoPanel.bufferInfoLeft = new ArrayList<>();
                    OverlayInfoPanel.bufferInfoRight = new ArrayList<>();
                }

            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
//                if (DebugUtils.targetEntity == null) {
//                    DebugUtils.startDebugFor(contextSupplier.get().getSender(), (Mob) contextSupplier.get().getSender().getLevel().getEntity(entityId));
//                } else {
//                    DebugUtils.stopDebug();
//                }
                DebugUtils.getDebuggableTarget(contextSupplier.get().getSender().level().getEntity(entityId)).ifPresent(
                        pathfinderMob -> {
                            DebugUtils.switchTracking(contextSupplier.get().getSender(), pathfinderMob);
                            IceAndFire.sendMSGToPlayer(new MessageDebugEntity(pathfinderMob.getId()),
                                                       contextSupplier.get().getSender());
                        });

            }
        });
        return true;
    }


}

