package com.github.alexthe666.iceandfire.entity.debug.quinnfrost;

import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.RenderEvent;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay.OverlayRenderEvent;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.events.DebuggerEventsClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class ExtendedEntityDebugger {
    public static boolean EXTENDED_DEBUG = true;

    public static void registerClient() {
        if (EXTENDED_DEBUG) {
            MinecraftForge.EVENT_BUS.register(RenderEvent.class);
            MinecraftForge.EVENT_BUS.register(new OverlayRenderEvent(Minecraft.getInstance()));
            MinecraftForge.EVENT_BUS.register(DebuggerEventsClient.class);
        }
    }

    public static void registerCommon() {
        if (EXTENDED_DEBUG) {

        }
    }

    public static void tickClient() {

    }

    public static void tickServer() {

    }
}
