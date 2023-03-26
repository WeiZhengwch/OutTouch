package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;

public class Velocity extends Mod {

    public Velocity() {
        super("Velocity", true);
    }

    public boolean isEnable() {
        return mc.gameSettings.velocityhori != 100 || mc.gameSettings.velocityvert != 100;
    }
}
