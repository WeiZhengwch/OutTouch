package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;

public class AutoClicker extends Mod {

    public AutoClicker() {
        super("AutoClicker", true);
    }

    public boolean isEnable() {
        return mc.gameSettings.autoclicker > 0;
    }
}
