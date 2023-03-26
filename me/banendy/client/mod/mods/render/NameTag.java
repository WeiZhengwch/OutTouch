package me.banendy.client.mod.mods.render;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;

public class NameTag extends Mod {

    public NameTag() {
        super("NameTag", Minecraft.getMinecraft().gameSettings.nametag);
    }

    public boolean isEnable() {
        return mc.gameSettings.nametag;
    }
}
