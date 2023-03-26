package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;

public class Reach extends Mod {

    public Reach() {
        super("Reach", true);
    }

    public boolean isEnable() {
        return mc.gameSettings.combatrange > 3.0;
    }
}
