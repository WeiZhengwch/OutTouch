package me.banendy.client.gui;

import me.banendy.client.MainClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class MainMenu extends GuiScreen {

    public static int fadeout = 63;

    @Override
    public void initGui() {
        int j = height / 4 + 48;
        buttonList.add(new GuiButton(0, width / 2 - 100, j, I18n.format("menu.singleplayer")));
        buttonList.add(new GuiButton(0, width / 2 - 100, j, I18n.format("menu.singleplayer")));
        buttonList.add(new GuiButton(1, width / 2 - 100, j + 24, I18n.format("menu.multiplayer")));
        buttonList.add(new GuiButton(2, width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options")));
        buttonList.add(new GuiButton(3, width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));
        buttonList.add(new GuiButton(4, 0, height - 20, 20, 20, false, "C"));
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0 -> mc.displayGuiScreen(new GuiSelectWorld(this));
            case 1 -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case 2 -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case 3 -> mc.shutdown();
            case 4 -> mc.displayGuiScreen(new ClientSetting(this, mc.gameSettings));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        FontRenderer font = mc.fontRendererObj;
        String text = MainClient.NAME + " " + MainClient.VERSION;
        font.drawString(text, width / 2 - font.getStringWidth(text) / 2, height / 2 - font.FONT_HEIGHT / 2 - 95, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (fadeout >= 0) {
            int out = fadeout * 4;
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/title/mojang.png"));
            GlStateManager.enableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(0.0D, height, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos((width - 256) / 2, height, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos((width - 256) / 2, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos((width + 256) / 2, height, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos(width, height, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos(width, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            worldrenderer.pos((width + 256) / 2, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, out).endVertex();
            tessellator.draw();
            mc.draw((width - 256) / 2, (height - 256) / 2, 0, 0, 256, 256, 255, 255, 255, out);
            fadeout--;
        } else {
            drawEridani(true, 0, height, 120, 195);
            drawAsphodene(true, width - 120, height, 120, 195);
        }
    }
}
