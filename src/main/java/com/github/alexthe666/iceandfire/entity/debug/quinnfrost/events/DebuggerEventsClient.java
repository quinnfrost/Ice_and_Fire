package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.events;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafKeybindRegistry;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.RayTraceUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.RenderNode;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay.OverlayInfoPanel;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageDebugEntity;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.Pathfinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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

    public static void scanDebugKeyPress(Player player) {
        if (IafKeybindRegistry.extended_debug.consumeClick()) {
            // Do raytrace
            HitResult result = RayTraceUtils.getTargetBlockOrEntity(player,
                                                                    256,
                                                                    entity -> entity instanceof LivingEntity || entity instanceof EntityMutlipartPart
            );
            if (result instanceof EntityHitResult) {
                // Select debug entity
                Entity entity = ((EntityHitResult) result).getEntity();
//                ClientGlow.setGlowing(mob, 10);
                if (player.level().isClientSide()) {
                    if (Pathfinding.isDebug()) {
//                        if (DebugUtils.isTracking(player, entity)) {
//                            DebugUtils.stopTracking(player);
//                        } else {
//                            if (entity instanceof PathfinderMob target || entity instanceof EntityMutlipartPart part) {
//                                DebugUtils.switchTracking(player, entity);
//                            }
//                        }
                        IceAndFire.sendMSGToServer(new MessageDebugEntity(entity.getId()));
                    }
                }

            }
        }
    }
}
