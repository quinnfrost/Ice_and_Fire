package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages;


import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay.OverlayCrossHair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageClientDisplay {

    private Vec2 pos = new Vec2(0, 0);
    private int displayTime = 20;
    private int size = 3;

    private List<String> message = new ArrayList<>();
    public MessageClientDisplay(Vec2 pos, int displayTime, List<String> message) {
        this.pos = pos;
        this.displayTime = displayTime;

        this.size = message.size();
        this.message = message;
    }

    public MessageClientDisplay(FriendlyByteBuf buffer) {
        this.pos = new Vec2(buffer.readFloat(), buffer.readFloat());
        this.displayTime = buffer.readInt();

        this.size = buffer.readInt();
        if (this.size > 0) {
            for (int i = 0; i < this.size; i++) {
                this.message.add(buffer.readUtf());
            }
        }
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeFloat(pos.x);
        buffer.writeFloat(pos.y);
        buffer.writeInt(displayTime);

        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            buffer.writeUtf(message.get(i));
        }
    }

    public boolean handler(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            contextSupplier.get().setPacketHandled(true);

            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                OverlayCrossHair.setCrossHairString(pos, message.get(0), displayTime, true);
            }

        });
        return true;
    }
}

