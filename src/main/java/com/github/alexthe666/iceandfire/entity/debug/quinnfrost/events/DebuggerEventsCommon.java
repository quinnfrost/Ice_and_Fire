package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.events;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.EntityCommander;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.ExtendedEntityDebugger;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageClientDisplay;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class DebuggerEventsCommon {
    public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityLiving() instanceof Player player && event.getItemStack().getItem() == IafItemRegistry.DRAGON_DEBUG_STICK.get()) {
            // Cancel event, debug stick is handled else where
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
//            // Do raytrace
//            HitResult result = RayTraceUtils.getTargetBlockOrEntity(player,
//                                                                    256,
//                                                                    entity -> entity instanceof LivingEntity || entity instanceof EntityMutlipartPart
//            );
//            if (result instanceof EntityHitResult) {
//                // Select debug entity
//                Entity entity = ((EntityHitResult) result).getEntity();
//                DebugUtils.getDebuggableTarget(entity).ifPresent(mob -> {
//                    ClientGlow.setGlowing(mob, 10);
//                    if (!event.getWorld().isClientSide()) {
//                        if (Pathfinding.isDebug()) {
////                        if (DebugUtils.isTracking(player, entity)) {
////                            DebugUtils.stopTracking(player);
////                        } else {
////                            if (entity instanceof PathfinderMob target || entity instanceof EntityMutlipartPart part) {
////                                DebugUtils.switchTracking(player, entity);
////                            }
////                        }
//                            IceAndFire.sendMSGToServer(new MessageDebugEntity(entity.getId()));
//                        }
//                    }
//
//                });
//            }
        }


    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getPlayer() != null && event.getItemStack().getItem() == IafItemRegistry.DRAGON_DEBUG_STICK.get()) {
            // Cancel event, debug stick is handled else where
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    public static void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntity().level.isClientSide) {
            if (ExtendedEntityDebugger.EXTENDED_DEBUG && event.getEntity() instanceof Player player && DebugUtils.isTracking(
                    player)) {
                DebugUtils.onTrackerUpdate(player);
                EntityCommander.onTrackerUpdate(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (ExtendedEntityDebugger.EXTENDED_DEBUG) {
            if (event.getEntity() instanceof PathfinderMob mob && DebugUtils.isTracking(mob)) {
                DebugUtils.getTrackingPlayer(mob).ifPresent(
                        player -> {
                            IceAndFire.sendMSGToPlayer(new MessageClientDisplay(
                                                               new Vec2(-20f, 30f),
                                                               20,
                                                               List.of(String.format("%.1f (%d)", event.getAmount(), event.getEntityLiving().getArmorValue()))
                                                       ),
                                                       (ServerPlayer) player
                            );
                        }
                );
            } else if (event.getSource().getEntity() instanceof LivingEntity sourceEntity && DebugUtils.isTracking(sourceEntity)) {
                DebugUtils.getTrackingPlayer(sourceEntity).ifPresent(
                        player -> {
                            IceAndFire.sendMSGToPlayer(new MessageClientDisplay(
                                                               new Vec2(20f, 30f),
                                                               20,
                                                               List.of(String.format("%.1f (%d)", event.getAmount(), event.getEntityLiving().getArmorValue()))
                                                       ),
                                                       (ServerPlayer) player
                            );
                        }
                );

            }
        }
    }

    public static void onLivingAttacked(final LivingAttackEvent event) {
        if (event.getEntity() instanceof PathfinderMob mob && DebugUtils.isTracking(mob)) {
            float xoffset = event.getSource() == DamageSource.GENERIC ? -40 : -20;
            DebugUtils.getTrackingPlayer(mob).ifPresent(
                    player -> {
                        IceAndFire.sendMSGToPlayer(new MessageClientDisplay(
                                                           new Vec2(xoffset, 40f),
                                                           20,
                                                           List.of(String.format("%.1f", event.getAmount()))
                                                   ),
                                                   (ServerPlayer) player
                        );
                    }
            );
        } else if (event.getSource().getEntity() instanceof LivingEntity sourceEntity && DebugUtils.isTracking(sourceEntity)) {
            DebugUtils.getTrackingPlayer(sourceEntity).ifPresent(
                    player -> {
                        IceAndFire.sendMSGToPlayer(new MessageClientDisplay(
                                                           new Vec2(20f, 40f),
                                                           20,
                                                           List.of(String.format("%.1f", event.getAmount()))
                                                   ),
                                                   (ServerPlayer) player
                        );
                    }
            );

        }
    }

}
