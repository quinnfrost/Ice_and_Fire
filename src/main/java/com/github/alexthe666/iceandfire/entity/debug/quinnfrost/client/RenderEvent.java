package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvent {
    @SubscribeEvent
    public static void renderWorldLastEvent(RenderLevelLastEvent event) {
        RenderNode.render(event.getPoseStack());
//        if (DragonTongue.isIafPresent) {
//            IafHelperClass.renderWorldLastEvent(event);
//        }
    }

}

