package me.banendy.client.mod.mods.movement;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;

public class NoSlow extends Mod {
    public NoSlow() {
        super("NoSlow", mc.gameSettings.noslow);
    }

    public boolean isEnable() {
        return mc.gameSettings.noslow;
    }
}
