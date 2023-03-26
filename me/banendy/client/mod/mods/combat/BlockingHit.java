package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;

public class BlockingHit extends Mod {
    public BlockingHit() {
        super("BlockingHit", mc.gameSettings.blockinghit);
    }

    public boolean isEnable() {
        return mc.gameSettings.blockinghit;
    }
}
