package com.github.alexthe666.iceandfire.entity.debug.quinnfrost.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OverlayInfoPanel {
    public static List<String> bufferInfoLeft = new ArrayList<>(3);
    public static List<String> bufferInfoRight = new ArrayList<>(3);

    public static void renderPanel(GuiGraphics ms) {
        if (bufferInfoLeft == null) {
            return;
        }

        final int maxLineLength = 50;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Font fontRender = minecraft.font;
        Color colour = new Color(255, 255, 255, 255);

//        renderString(ms, bufferInfoLeft, colour, 5, 5, maxLineLength, false);
        renderDifference(ms);
    }

    public static void renderString(GuiGraphics ms, List<String> content, Color color, int xOffset, int yOffset, int maxLineLength, boolean rightAlign) {
        if (content == null) {
            return;
        }
        Font fontRenderer = Minecraft.getInstance().font;
        int lineHeight = 10;
        int sublineHeight = 8;

        int sublineCount = 0;
        if (!rightAlign) {
            for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
                String currentString = content.get(lineIndex);
                if (currentString.length() < maxLineLength) {
                    fontRenderer.drawInBatch(currentString,
                                             xOffset,
                                             yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                             color.getRGB(),
                                             false,
                                             ms.pose().last().pose(),
                                             ms.bufferSource(),
                                             Font.DisplayMode.NORMAL,
                                             0,
                                             15728880
                    );
                } else {
                    while (currentString.length() >= maxLineLength) {
                        fontRenderer.drawInBatch(
                                currentString.substring(0, maxLineLength),
                                xOffset,
                                yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                color.getRGB(),
                                false,
                                ms.pose().last().pose(),
                                ms.bufferSource(),
                                Font.DisplayMode.NORMAL,
                                0,
                                15728880
                        );
                        currentString = currentString.substring(maxLineLength);
                        sublineCount += 1;
                    }
                    fontRenderer.drawInBatch(currentString,
                                             xOffset,
                                             yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                             color.getRGB(),
                                             false,
                                             ms.pose().last().pose(),
                                             ms.bufferSource(),
                                             Font.DisplayMode.NORMAL,
                                             0,
                                             15728880
                    );
                }
            }
        } else {
            for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
                String currentString = content.get(lineIndex);
                fontRenderer.width(currentString);
                if (currentString.length() < maxLineLength) {
                    fontRenderer.drawInBatch(currentString,
                                             xOffset - fontRenderer.width(currentString),
                                             yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                             color.getRGB(),
                                             false,
                                             ms.pose().last().pose(),
                                             ms.bufferSource(),
                                             Font.DisplayMode.NORMAL,
                                             0,
                                             15728880
                    );
                } else {
                    while (currentString.length() >= maxLineLength) {
                        String currentRenderString = currentString.substring(0, maxLineLength - 1);
                        fontRenderer.drawInBatch(currentRenderString,
                                                 xOffset - fontRenderer.width(currentRenderString),
                                                 yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                                 color.getRGB(),
                                                 false,
                                                 ms.pose().last().pose(),
                                                 ms.bufferSource(),
                                                 Font.DisplayMode.NORMAL,
                                                 0,
                                                 15728880
                        );
                        currentString = currentString.substring(maxLineLength);
                        sublineCount += 1;
                    }
                    fontRenderer.drawInBatch(currentString,
                                             xOffset - fontRenderer.width(currentString),
                                             yOffset + lineHeight * lineIndex + sublineHeight * sublineCount,
                                             color.getRGB(),
                                             false,
                                             ms.pose().last().pose(),
                                             ms.bufferSource(),
                                             Font.DisplayMode.NORMAL,
                                             0,
                                             15728880
                    );
                }
            }
        }

    }

    public static void renderDifference(GuiGraphics ms) {
        if (bufferInfoLeft == null) {
            return;
        }
        final int xOffsetLeft = 5;
        final int yOffsetLeft = 5;
        final int xOffsetRight = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        final int yOffsetRight = 5;
        final int maxLineLength = 60;

        Font fontRenderer = Minecraft.getInstance().font;
        int lineHeight = 10;
        int sublineHeight = 8;

        int sublineCountLeft = 0;
        int sublineCountRight = 0;
        if (bufferInfoLeft.size() == bufferInfoRight.size()) {
            for (int lineIndex = 0; lineIndex < bufferInfoLeft.size(); lineIndex++) {
                Color color = Color.WHITE;

                String currentStringLeft = bufferInfoLeft.get(lineIndex);
                if (currentStringLeft.length() < maxLineLength) {
                    fontRenderer.drawInBatch(currentStringLeft,
                                      xOffsetLeft,
                                      yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft,
                                      color.getRGB(),
                                      false,
                                      ms.pose().last().pose(),
                                      ms.bufferSource(),
                                      Font.DisplayMode.NORMAL,
                                      0,
                                      15728880
                    );
                } else {
                    while (currentStringLeft.length() >= maxLineLength) {
                        fontRenderer.drawInBatch(
                                          currentStringLeft.substring(0, maxLineLength - 1),
                                          xOffsetLeft,
                                          yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft,
                                          color.getRGB(),
                                          false,
                                          ms.pose().last().pose(),
                                          ms.bufferSource(),
                                          Font.DisplayMode.NORMAL,
                                          0,
                                          15728880
                        );
                        currentStringLeft = currentStringLeft.substring(maxLineLength);
                        sublineCountLeft += 1;
                    }
                    fontRenderer.drawInBatch(
                                      currentStringLeft,
                                      xOffsetLeft,
                                      yOffsetLeft + lineHeight * lineIndex + sublineHeight * sublineCountLeft,
                                      color.getRGB(),
                                      false,
                                      ms.pose().last().pose(),
                                      ms.bufferSource(),
                                      Font.DisplayMode.NORMAL,
                                      0,
                                      15728880
                    );
                }

                String currentStringRight = bufferInfoRight.get(lineIndex);
                if (!bufferInfoLeft.get(lineIndex).equals(bufferInfoRight.get(lineIndex))) {
                    color = Color.RED;
                }
                if (currentStringRight.length() < maxLineLength) {
                    fontRenderer.drawInBatch(
                                      currentStringRight,
                                      xOffsetRight - fontRenderer.width(currentStringRight),
                                      yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight,
                                      color.getRGB(),
                                      false,
                                      ms.pose().last().pose(),
                                      ms.bufferSource(),
                                      Font.DisplayMode.NORMAL,
                                      0,
                                      15728880
                    );
                } else {
                    while (currentStringRight.length() >= maxLineLength) {
                        String currentRenderString = currentStringRight.substring(0, maxLineLength - 1);
                        fontRenderer.drawInBatch(
                                          currentRenderString,
                                          xOffsetRight - fontRenderer.width(currentRenderString),
                                          yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight,
                                          color.getRGB(),
                                          false,
                                          ms.pose().last().pose(),
                                          ms.bufferSource(),
                                          Font.DisplayMode.NORMAL,
                                          0,
                                          15728880
                        );
                        currentStringRight = currentStringRight.substring(maxLineLength);
                        sublineCountRight += 1;
                    }
                    fontRenderer.drawInBatch(
                                      currentStringRight,
                                      xOffsetRight - fontRenderer.width(currentStringRight),
                                      yOffsetRight + lineHeight * lineIndex + sublineHeight * sublineCountRight,
                                      color.getRGB(),
                                      false,
                                      ms.pose().last().pose(),
                                      ms.bufferSource(),
                                      Font.DisplayMode.NORMAL,
                                      0,
                                      15728880
                    );
                }

            }
        } else {
            renderString(ms, bufferInfoLeft, Color.WHITE, xOffsetLeft, yOffsetLeft, maxLineLength, false);
            renderString(ms, bufferInfoRight, Color.WHITE, xOffsetRight, yOffsetRight, maxLineLength, true);
        }


    }

}

