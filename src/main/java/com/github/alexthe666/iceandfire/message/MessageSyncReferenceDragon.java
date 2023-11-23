package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.gui.GuiDragon;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Server send reference mob to client, or client request the relevant mob
 */
public class MessageSyncReferenceDragon {
    private int entityID;
    private Collection<MobEffectInstance> effects;

    public MessageSyncReferenceDragon() {
        this.entityID = -1;
        this.effects = new ArrayList<>();
    }

    /**
     * Server send reference mob to client
     *
     * @param dragon entity dragon base
     */
    public MessageSyncReferenceDragon(EntityDragonBase dragon) {
        this.entityID = dragon == null ? 0 : dragon.getId();
        this.effects = dragon == null ? new ArrayList<>() : dragon.getActiveEffects();
    }

    /**
     * Client request the relevant mob
     *
     * @param entity a dragon, or its part
     */
    public MessageSyncReferenceDragon(Entity entity) {
        this.entityID = entity == null ? -1 : entity.getId();
        this.effects = new ArrayList<>();
    }

    /**
     * Message constructor
     *
     * @param entityID
     * @param effects
     */
    public MessageSyncReferenceDragon(int entityID, Collection<MobEffectInstance> effects) {
        this.entityID = entityID;
        this.effects = effects;
    }


    public static MessageSyncReferenceDragon decoder(FriendlyByteBuf buffer) {
        int entityID = buffer.readInt();
        Collection<MobEffectInstance> effects = buffer.readCollection(ArrayList::new, friendlyByteBuf -> {
            return MobEffectInstance.load(friendlyByteBuf.readNbt());
        });
        return new MessageSyncReferenceDragon(entityID, effects);
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
        buffer.writeCollection(effects, (friendlyByteBuf, mobEffectInstance) -> {
            friendlyByteBuf.writeNbt(mobEffectInstance.save(new CompoundTag()));
        });

    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Entity entity = IceAndFire.PROXY.getClientSidePlayer().level.getEntity(entityID);
                if (entity instanceof EntityDragonBase) {
                    GuiDragon.referencedDragon = (EntityDragonBase) entity;
                    Iterator<MobEffectInstance> iterator = GuiDragon.referencedDragon.getActiveEffects().iterator();

                    boolean flag;
                    for (flag = false; iterator.hasNext(); flag = true) {
                        MobEffectInstance effect = iterator.next();
                        iterator.remove();
                    }
                    for (MobEffectInstance effect :
                            effects) {
                        GuiDragon.referencedDragon.forceAddEffect(effect, null);
                    }
                }
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                if (contextSupplier.get().getSender() != null) {
                    Entity entity = contextSupplier.get().getSender().level.getEntity(entityID);
                    if (entity instanceof EntityMutlipartPart part) {
                        entity = part.getRootParent();
                    }
                    if (entity instanceof EntityDragonBase dragon) {
                        IceAndFire.sendMSGToPlayer(new MessageSyncReferenceDragon(dragon),
                                                   contextSupplier.get().getSender()
                        );
                    }

                }
            }
        });
        return true;
    }
}
