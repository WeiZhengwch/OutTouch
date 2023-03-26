package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;

public class FastPlace extends Mod {

    public FastPlace() {
        super("FastPlace", true);
    }

    public boolean isEnable() {
        return mc.gameSettings.fastplace < 5;
    }
}
