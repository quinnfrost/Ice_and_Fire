package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.events;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.DebugUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.ExtendedEntityDebugger;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.RayTraceUtils;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.ClientGlow;
import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.messages.MessageDebugEntity;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.Pathfinding;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DebuggerEventsCommon {
    public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() != null && event.getItemStack().getItem() == IafItemRegistry.DRAGON_DEBUG_STICK.get()) {
            Player player = event.getEntity();
            // Do raytrace
            HitResult result = RayTraceUtils.getTargetBlockOrEntity(player,
                                                                    256,
                                                                    entity -> entity instanceof LivingEntity || entity instanceof EntityMutlipartPart
            );
            if (result instanceof EntityHitResult) {
                // Select debug entity
                Entity entity = ((EntityHitResult) result).getEntity();
                DebugUtils.getDebuggableTarget(entity).ifPresent(mob -> {
                    ClientGlow.setGlowing(mob, 10);
                    if (!event.getLevel().isClientSide()) {
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

                });
            }
        }


    }

    public static void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide) {
            if (ExtendedEntityDebugger.EXTENDED_DEBUG && event.getEntity() instanceof Player player && DebugUtils.isTracking(
                    player)) {
                DebugUtils.onTrackerUpdate(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (ExtendedEntityDebugger.EXTENDED_DEBUG) {
            DebugUtils.onEntityDamage(event);
        }
    }
}
