package me.banendy.client.mod.mods.render;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;

public class ViewClip extends Mod {
    public ViewClip() {
        super("ViewClip", mc.gameSettings.viewclip);
    }

    public boolean isEnable() {
        return mc.gameSettings.nametag;
    }
}

