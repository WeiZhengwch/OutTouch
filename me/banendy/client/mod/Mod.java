package me.banendy.client.mod;

import me.banendy.client.misc.Cache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;

public class Mod {
    private final String name;
    protected static Minecraft mc = Minecraft.getMinecraft();
    protected boolean enable;
    private int key;

    public Mod(String name, boolean enable) {
        this.name = name;
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void render() {
    }

    public void update() {
    }
}

