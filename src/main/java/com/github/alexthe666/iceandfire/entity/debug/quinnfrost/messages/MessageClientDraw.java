package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages;

import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.RenderNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClientDraw {
    private int time = 2;
    private Vec3 pos;
    private Vec3 start;
    private Integer index;

    public MessageClientDraw(Integer index, Vec3 pos, Vec3 start) {
        this.index = index;
        this.pos = pos;
        this.start = start;
    }

    public static MessageClientDraw decoder(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return new MessageClientDraw(buffer.readInt(), new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ), new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ));
        } else {
            return new MessageClientDraw(null, new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ), new Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ));
        }
    }

    public void encoder(FriendlyByteBuf buffer) {
        if (index != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(index);
        } else {
            buffer.writeBoolean(false);
            buffer.writeInt(0);
        }

        buffer.writeDouble(pos.x);
        buffer.writeDouble(pos.y);
        buffer.writeDouble(pos.z);

        buffer.writeDouble(start.x);
        buffer.writeDouble(start.y);
        buffer.writeDouble(start.z);
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                RenderNode.setRenderPos(2 * 20, pos, start, index);
            } else if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {

            }
        });
        return true;
    }

}

