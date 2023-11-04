package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class OverlayRenderEvent extends Gui {
    public OverlayRenderEvent(Minecraft minecraft) {
        super(minecraft, minecraft.getItemRenderer());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!minecraft.options.renderDebug) {
            OverlayInfoPanel.renderPanel(event.getGuiGraphics());
        }
        OverlayCrossHair.renderStringCrossHair(event.getGuiGraphics());

    }

}
