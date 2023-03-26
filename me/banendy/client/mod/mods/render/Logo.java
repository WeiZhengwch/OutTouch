package me.banendy.client.mod.mods.render;

import me.banendy.client.MainClient;
import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class Logo extends Mod {
    public Logo() {
        super("Logo", true);
    }

    @Override
    public void render() {
        String text = MainClient.NAME + " " + MainClient.VERSION;
        mc.fontRendererObj.drawString(text, 2, 11, new Color(224, 224, 224).getRGB());
    }

}
