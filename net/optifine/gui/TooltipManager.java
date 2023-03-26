package net.optifine.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TooltipManager {
    private final GuiScreen guiScreen;
    private final TooltipProvider tooltipProvider;
    private int lastMouseX;
    private int lastMouseY;
    private long mouseStillTime;

    public TooltipManager(GuiScreen guiScreen, TooltipProvider tooltipProvider) {
        this.guiScreen = guiScreen;
        this.tooltipProvider = tooltipProvider;
    }

    public void drawTooltips(int x, int y, List buttonList) {
        if (Math.abs(x - lastMouseX) <= 5 && Math.abs(y - lastMouseY) <= 5) {
            int i = 700;

            if (System.currentTimeMillis() >= mouseStillTime + (long) i) {
                GuiButton guibutton = GuiScreenOF.getSelectedButton(x, y, buttonList);

                if (guibutton != null) {
                    Rectangle rectangle = tooltipProvider.getTooltipBounds(guiScreen, x, y);
                    String[] astring = tooltipProvider.getTooltipLines(guibutton, rectangle.width);

                    if (astring != null) {
                        if (astring.length > 8) {
                            astring = Arrays.copyOf(astring, 8);
                            astring[astring.length - 1] = astring[astring.length - 1] + " ...";
                        }

                        if (tooltipProvider.isRenderBorder()) {
                            int j = -528449408;
                            drawRectBorder(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, j);
                        }

                        Gui.drawRect(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, -536870912);

                        for (int l = 0; l < astring.length; ++l) {
                            String s = astring[l];
                            int k = 14540253;

                            if (s.endsWith("!")) {
                                k = 16719904;
                            }

                            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
                            fontrenderer.drawStringWithShadow(s, (float) (rectangle.x + 5), (float) (rectangle.y + 5 + l * 11), k);
                        }
                    }
                }
            }
        } else {
            lastMouseX = x;
            lastMouseY = y;
            mouseStillTime = System.currentTimeMillis();
        }
    }

    private void drawRectBorder(int x1, int y1, int x2, int y2, int col) {
        Gui.drawRect(x1, y1 - 1, x2, y1, col);
        Gui.drawRect(x1, y2, x2, y2 + 1, col);
        Gui.drawRect(x1 - 1, y1, x1, y2, col);
        Gui.drawRect(x2, y1, x2 + 1, y2, col);
    }
}