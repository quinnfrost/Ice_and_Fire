package com.github.alexthe666.iceandfire.event;

import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class PlayerRenderEvents {
    public ResourceLocation redTex = new ResourceLocation("iceandfire", "textures/models/misc/cape_fire.png");
    public ResourceLocation redElytraTex = new ResourceLocation("iceandfire", "textures/models/misc/elytra_fire.png");
    public ResourceLocation blueTex = new ResourceLocation("iceandfire", "textures/models/misc/cape_ice.png");
    public ResourceLocation blueElytraTex = new ResourceLocation("iceandfire", "textures/models/misc/elytra_ice.png");
    public ResourceLocation betaTex = new ResourceLocation("iceandfire", "textures/models/misc/cape_beta.png");
    public ResourceLocation betaElytraTex = new ResourceLocation("iceandfire", "textures/models/misc/elytra_beta.png");

    public UUID[] redcapes = new UUID[]{
            /* zeklo */UUID.fromString("59efccaf-902d-45da-928a-5a549b9fd5e0"),
            /* Alexthe666 */UUID.fromString("71363abe-fd03-49c9-940d-aae8b8209b7c")
    };
    public UUID[] bluecapes = new UUID[]{
            /* Raptorfarian */UUID.fromString("0ed918c8-d612-4360-b711-cd415671356f"),
            /*Zyranna*/        UUID.fromString("5d43896a-06a0-49fb-95c5-38485c63667f")};
    public UUID[] betatesters = new UUID[]{
    };

    @SubscribeEvent
    public void playerRender(RenderPlayerEvent.Pre event) {
        //TODO
        /*
        if (event.getEntityLiving() instanceof AbstractClientPlayerEntity) {
                NetworkPlayerInfo info = ((AbstractClientPlayerEntity)event.getEntityLiving()).getPlayerInfo();
            if (info != null) {
                Map<Type, ResourceLocation> textureMap = info.playerTextures;
                if (textureMap != null) {
                    if (hasBetaCape(event.getEntityLiving().getUniqueID())) {
                        textureMap.put(Type.CAPE, betaTex);
                        textureMap.put(Type.ELYTRA, betaElytraTex);
                    }
                    if (hasRedCape(event.getEntityLiving().getUniqueID())) {
                        textureMap.put(Type.CAPE, redTex);
                        textureMap.put(Type.ELYTRA, redElytraTex);
                    }
                    if (hasBlueCape(event.getEntityLiving().getUniqueID())) {
                        textureMap.put(Type.CAPE, blueTex);
                        textureMap.put(Type.ELYTRA, blueElytraTex);
                    }
                }
            }
        }*/
        if (event.getEntity().getUUID().equals(ServerEvents.ALEX_UUID)) {
            event.getPoseStack().pushPose();
            float f2 = ((float) event.getEntity().tickCount - 1 + event.getPartialTick());
            float f3 = Mth.sin(f2 / 10.0F) * 0.1F + 0.1F;
            event.getPoseStack().translate((float) 0, event.getEntity().getBbHeight() * 1.25F, (float) 0);
            float f4 = (f2 / 20.0F) * (180F / (float) Math.PI);
            event.getPoseStack().mulPose(Axis.YP.rotationDegrees(f4));
            event.getPoseStack().pushPose();
            Minecraft.getInstance().getItemRenderer().renderStatic(Minecraft.getInstance().player, new ItemStack(IafItemRegistry.WEEZER_BLUE_ALBUM.get()), ItemDisplayContext.GROUND, false, event.getPoseStack(), event.getMultiBufferSource(), event.getEntity().level(), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0);
            event.getPoseStack().popPose();
            event.getPoseStack().popPose();

        }
    }

    private boolean hasRedCape(UUID uniqueID) {
        for (UUID uuid1 : redcapes) {
            if (uniqueID.equals(uuid1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBlueCape(UUID uniqueID) {
        for (UUID uuid1 : bluecapes) {
            if (uniqueID.equals(uuid1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBetaCape(UUID uniqueID) {
        for (UUID uuid1 : betatesters) {
            if (uniqueID.equals(uuid1)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void computeFovModifierEvent(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player.getVehicle() instanceof EntityDragonBase dragon) {
            // vanilla sprinting has a fov mod of 1.15, and is applied here
            if (dragon.isFlying() || dragon.isHovering()) {
                event.setNewFovModifier(1f + (float) Mth.map(
                        Math.max(dragon.getDeltaMovement().lengthSqr(), 0d),
                        1*1d,
                        3*3d,
                        0d,
                        0.85d
                ));
            }
        } else if (player.getVehicle() instanceof EntityAmphithere amphithere) {
            if (amphithere.isFlying() || amphithere.isHovering()) {
                event.setNewFovModifier(1f + (float) Mth.map(
                        Math.max(amphithere.getDeltaMovement().lengthSqr(), 0d),
                        0.45*0.45d,
                        2.2*2.2d,
                        0d,
                        0.85d
                ));
            }
        }


    }

    @SubscribeEvent
    public void computeFov(ViewportEvent.ComputeFov event) {

    }
}
