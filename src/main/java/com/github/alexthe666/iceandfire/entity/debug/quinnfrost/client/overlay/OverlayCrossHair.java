package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OverlayCrossHair {
//    public static ResourceLocation markTexture = new ResourceLocation("dragontongue", "textures/gui/mark.png");
//    public static ResourceLocation scopeTexture = new ResourceLocation("dragontongue", "textures/misc/scope.png");
    public static boolean renderScope = false;
    public static float scopeSuggestion = 0;
    public static delayedTimer timer = new delayedTimer();

    private static Map<Vec2, Pair<Integer, String>> bufferStringMap = new HashMap<Vec2, Pair<Integer, String>>() {
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
    };
    private static Map<Vec2, Pair<Integer, IconType>> bufferIconMap = new HashMap<>();
    private static final Object lock = new Object();

    public enum IconType {
        HIT,
        CRITICAL,
        WARN,
        TARGET
    }

    public static class delayedTimer extends Thread {
        public delayedTimer() {

        }

        public void run() {
            while (
                    !bufferIconMap.isEmpty()
                            || !bufferStringMap.isEmpty()
            ) {
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    bufferStringMap.replaceAll((vector3i, integerStringPair) -> {
                        if (integerStringPair.getFirst() >= 0) {
                            return Pair.of(integerStringPair.getFirst() - 1, integerStringPair.getSecond());
                        }
                        return integerStringPair;
                    });
                    bufferStringMap.entrySet().removeIf(vector3iPairEntry -> vector3iPairEntry.getValue().getFirst() <= 0);

                    bufferIconMap.replaceAll((Vec2, integerIconTypePair) -> {
                        if (integerIconTypePair.getFirst() >= 0) {
                            return Pair.of(integerIconTypePair.getFirst() - 1, integerIconTypePair.getSecond());
                        }
                        return integerIconTypePair;
                    });
                    bufferIconMap.entrySet().removeIf(Vec2PairEntry -> Vec2PairEntry.getValue().getFirst() <= 0);
                }
            }
        }
    }

    /**
     * Set cross-hair display
     *
     * @param string     Content to display
     * @param stringTime Time before the content disappears in ticks
     * @param iconTime   Time before the cross-hair disappears in ticks, 0 for not showing at all
     * @param type
     * @param force      Whether to refresh the display time even if content is the same
     */
    public static void setCrossHairDisplay(@Nullable String string, int stringTime, int iconTime, @Nullable IconType type, boolean force) {
        setCrossHairIcon(new Vec2(0, 0), iconTime, type);
        setCrossHairString(new Vec2(-2, -40), string, stringTime, force);
    }

    public static void setCrossHairString(Vec2 offsetFromCrosshair, @Nullable String string, int stringTime, boolean force) {
        synchronized (lock) {
            if (string != null) {

                if (!force && string.equals(bufferStringMap.getOrDefault(offsetFromCrosshair,
                                                                         Pair.of(0, null)
                ).getSecond())) {
                    return;
                }
                if (!string.isEmpty() && stringTime != 0) {
                    bufferStringMap.put(offsetFromCrosshair, Pair.of(stringTime, string));
                }
            } else {
//                bufferStringMap.put(offsetFromCrosshair, Pair.of(0, ""));
//                return;
            }
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void setScopeString(String string) {

    }

    public static void setCrossHairIcon(Vec2 position, int iconTime, @Nullable IconType type) {
        if (iconTime != 0 || type == null) {
//            crIconTime = iconTime;
//            crIconType = type;
            bufferIconMap.put(position, Pair.of(iconTime, type));
        }

        if (!timer.isAlive()) {
            timer = new delayedTimer();
            timer.start();
        }
    }

    public static void renderStringCrossHair(GuiGraphics ms) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Font fontRender = minecraft.font;
        Color colour = new Color(255, 255, 255, 255);

//        int width = event.getWindow().getScaledWidth();
//        int height = event.getWindow().getScaledHeight();
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        synchronized (lock) {
            if (!bufferStringMap.isEmpty()) {
                bufferStringMap.forEach((Vec2, integerStringPair) -> {
                    fontRender.drawInBatch(integerStringPair.getSecond(), scaledWidth / 2.0f + Vec2.x, scaledHeight / 2.0f + Vec2.y, colour.getRGB(),                                             false,
                                           ms.pose().last().pose(),
                                           ms.bufferSource(),
                                           Font.DisplayMode.NORMAL,
                                           0,
                                           15728880);
                });
            }
        }
    }

//    public static void renderScope(PoseStack ms) {
//        if (!renderScope) {
//            return;
//        }
//        int scopeTextureLength = 256;
//        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//
//        RenderSystem.setShaderTexture(0, scopeTexture);
//
//        GuiComponent.blit(ms, scaledWidth / 2 - scopeTextureLength / 2, scaledHeight / 2, 0, 0, 0, scopeTextureLength, scopeTextureLength, 256, 256);
////        GuiUtils.drawTexturedModalRect(ms, scaledWidth / 2 - scopeTextureLength / 2, scaledHeight / 2, 0, 0, scopeTextureLength, scopeTextureLength, 1);
//    }

//    public static void renderScopeSuggestionQuad(PoseStack poseStack) {
//        final int lineWidth = 1;
//        final float suggestionWidth = 40;
//        float suggestPos = (float) (0.4058604333 * Math.pow(40, 1.395441973));
//
//
//        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        PoseStack posestack = RenderSystem.getModelViewStack();
//        posestack.pushPose();
//        posestack.translate((double) (scaledWidth / 2), (double) (scaledHeight / 2), (double) 0);
//        posestack.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
//        posestack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
//        posestack.scale(-1.0F, -1.0F, -1.0F);
//        RenderSystem.applyModelViewMatrix();
//
//        double pLineLength = 10;
//
//        GlStateManager._disableTexture();
//        GlStateManager._depthMask(false);
//        GlStateManager._disableCull();
//        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
//        Tesselator tesselator = RenderSystem.renderThreadTesselator();
//        BufferBuilder bufferbuilder = tesselator.getBuilder();
//
//        RenderSystem.lineWidth(2.0F);
//        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
//
//        bufferbuilder
//                .vertex(0.0D, 0.0D, 0.0D)
//                .color(255, 0, 0, 255)
//                .normal(1.0F, 0.0F, 0.0F)
//                .endVertex();
//        bufferbuilder
//                .vertex((double) pLineLength, 0.0D, 0.0D)
//                .color(255, 0, 0, 255)
//                .normal(1.0F,
//                        0.0F,
//                        0.0F
//                ).endVertex();
//
//        bufferbuilder
//                .vertex(0.0D, 0.0D, 0.0D)
//                .color(0, 255, 0, 255)
//                .normal(0.0F, 1.0F, 0.0F)
//                .endVertex();
//        bufferbuilder
//                .vertex(0.0D, (double) pLineLength, 0.0D)
//                .color(0, 255, 0, 255)
//                .normal(0.0F,
//                        1.0F,
//                        0.0F
//                ).endVertex();
//
//        bufferbuilder
//                .vertex(0.0D, 0.0D, 0.0D)
//                .color(127, 127, 255, 255)
//                .normal(0.0F, 0.0F, 1.0F)
//                .endVertex();
//        bufferbuilder
//                .vertex(0.0D, 0.0D, (double) pLineLength)
//                .color(127, 127, 255, 255)
//                .normal(0.0F,
//                        0.0F,
//                        1.0F
//                ).endVertex();
//
//        tesselator.end();
//
//        bufferbuilder.end();
//        BufferUploader.reset();
//
//
//    }

    public static void renderScopeSuggestion(PoseStack ms) {
        if (!renderScope) {
            return;
        }
        final float suggestionWidth = 40;

        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        float suggestPos = (float) (0.4058604333 * Math.pow(scopeSuggestion, 1.395441973));
        Color color = Color.WHITE;

        ms.pushPose();

        RenderBuffers renderBuffers = new RenderBuffers();
        MultiBufferSource.BufferSource pBufferSource = renderBuffers.bufferSource();
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.lines());

        PoseStack.Pose pose = ms.last();
        vertexconsumer.vertex(scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .normal(1.0f, 0.0f, 0.0f)
                .endVertex();

        vertexconsumer.vertex(scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .normal(1.0f, 0.0f, 0.0f)
                .endVertex();

        pBufferSource.endBatch(RenderType.lines());

//        RenderSystem.enableDepthTest();
//        RenderSystem.disableTexture();
//        RenderSystem.disableBlend();
//
//        final Tesselator tessellator = Tesselator.getInstance();
//        final BufferBuilder vertexBuffer = tessellator.getBuilder();
//
//        RenderSystem.lineWidth(2.0F);
////        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
//        vertexBuffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
////        vertexBuffer.vertex(0.5 + scaledWidth / 2f - suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0f, 0f, 0f, 1.0f).endVertex();
////        vertexBuffer.vertex(0.5 + scaledWidth / 2f + suggestionWidth / 2, 0.5 + scaledHeight / 2f + suggestPos, 0).color(0f, 0f, 0f, 1.0f).endVertex();
////        tessellator.end();
//
////        vertexBuffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
//        vertexBuffer.vertex(scaledWidth / 2f - suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
//        vertexBuffer.vertex(scaledWidth / 2f + suggestionWidth / 2, scaledHeight / 2f + suggestPos, 0).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
//        tessellator.end();
//
//        RenderSystem.lineWidth(1.0F);
//        RenderSystem.disableDepthTest();
        ms.popPose();
    }

//    public static void renderIconCrossHair(PoseStack ms) {
//        synchronized (lock) {
//            if (!bufferIconMap.isEmpty()) {
//                bufferIconMap.forEach((Vec2, integerIconTypePair) -> {
//                    Minecraft minecraft = Minecraft.getInstance();
//                    int markTextureLength = 16;
//                    int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
//                    int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
//                    int xOffset = 0;
//                    int yOffset = 0;
//
//                    int xPosition = (int) (scaledWidth / 2 - markTextureLength / 2 + Vec2.x);
//                    int yPosition = (int) (scaledHeight / 2 - markTextureLength / 2 + Vec2.y);
//                    minecraft.getTextureManager().bindForSetup(markTexture);
//                    switch (integerIconTypePair.getSecond()) {
//
//                        case HIT:
//                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, 0, 0, markTextureLength, markTextureLength, 1);
//                            break;
//                        case CRITICAL:
//                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength, 0, markTextureLength, markTextureLength, 1);
//                            break;
//                        case WARN:
//                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength * 2, 0, markTextureLength, markTextureLength, 1);
//                            break;
//                        case TARGET:
//                            GuiUtils.drawTexturedModalRect(ms, xPosition, yPosition, markTextureLength * 3, 0, markTextureLength, markTextureLength, 1);
//                            break;
//                    }
//                });
//            }
//        }
//
////        if (crIconTime > 0) {
//
////
////        }
//    }

}

