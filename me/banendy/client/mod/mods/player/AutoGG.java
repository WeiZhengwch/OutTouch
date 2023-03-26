package me.banendy.client.mod.mods.player;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;

public class AutoGG extends Mod {

    public AutoGG() {
        super("AutoGG", mc.gameSettings.autogg);
    }

    public boolean isEnable() {
        return mc.gameSettings.autogg;
    }
}
