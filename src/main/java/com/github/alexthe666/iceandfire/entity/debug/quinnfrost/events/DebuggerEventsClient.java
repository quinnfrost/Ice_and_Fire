package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.events;

import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.RenderNode;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay.OverlayInfoPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DebuggerEventsClient {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        int maxDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
        LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;

        if (!OverlayInfoPanel.bufferInfoLeft.isEmpty() && clientPlayerEntity != null && clientPlayerEntity.isPassenger()) {
            LivingEntity riding = (LivingEntity) clientPlayerEntity.getVehicle();
            if (riding != null) {
                RenderNode.setRenderPos(
                        2,
                        clientPlayerEntity.getPosition(1.0f).add(0,
                                                                 clientPlayerEntity.getEyeHeight(),
                                                                 0
                        ).add(riding.getDeltaMovement().scale(2f)),
                        clientPlayerEntity.getPosition(1.0f),
                        null
                );
            }
        }

//        if (clientPlayerEntity != null && clientPlayerEntity.isShiftKeyDown() && (clientPlayerEntity.getItemInHand(
//                InteractionHand.MAIN_HAND).getItem() == IafItemRegistry.DRAGON_BOW.get()
//                || clientPlayerEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.BOW)) {
//            OverlayCrossHair.renderScope = true;
//            HitResult rayTraceResult = util.getTargetBlockOrEntity(clientPlayerEntity, maxDistance, null);
//            if (rayTraceResult.getType() != HitResult.Type.MISS) {
//                double distance = clientPlayerEntity.getLightProbePosition(1.0f).distanceTo(rayTraceResult.getLocation());
//                OverlayCrossHair.scopeSuggestion = (float) distance;
//                OverlayCrossHair.setCrossHairString(
//                        Vector2f.CR_DISTANCE,
//                        String.format("%.1f", distance),
//                        2,
//                        true
//                );
//            } else {
//                OverlayCrossHair.setCrossHairString(
//                        Vector2f.CR_DISTANCE,
//                        "--",
//                        2,
//                        true
//                );
//            }
//        } else {
//            if (OverlayCrossHair.renderScope) {
//                // Clear text is not implemented
////                OverlayCrossHair.setCrossHairString(
////                        Vector2f.CR_DISTANCE,
////                        null,
////                        1,
////                        true
////                );
//                OverlayCrossHair.renderScope = false;
//            }
//        }

        ClientGlow.tickGlowing();
    }

}
