package me.banendy.client.mod;

import me.banendy.client.mod.mods.combat.*;
import me.banendy.client.mod.mods.movement.Eagle;
import me.banendy.client.mod.mods.movement.NoSlow;
import me.banendy.client.mod.mods.movement.Sprint;
import me.banendy.client.mod.mods.player.AutoGG;
import me.banendy.client.mod.mods.player.AutoTool;
import me.banendy.client.mod.mods.render.*;

import java.util.ArrayList;
import java.util.List;

public class ModManager {
    private final List<Mod> mods = new ArrayList<>();

    public List<Mod> getMods() {
        return mods;
    }

    public List<Mod> getEnableMods() {
        List<Mod> enableMods = new ArrayList<>();
        for (Mod enablemod : mods) {
            if (enablemod.isEnable()) {
                enableMods.add(enablemod);
            }
        }
        return enableMods;
    }

    public void onKey(int key) {
        for (Mod enableMod : getMods()) {
            if (enableMod.getKey() == key) {
                enableMod.setEnable(!enableMod.isEnable());
            }
        }
    }

    public void load() {
        mods.add(new Logo());
        mods.add(new Sprint());
        mods.add(new ModulesList());
        mods.add(new Particles());
        mods.add(new Animations());
        mods.add(new NameTag());
        mods.add(new FastPlace());
        mods.add(new Velocity());
        mods.add(new ViewClip());
        mods.add(new AutoGG());
        mods.add(new Reach());
        mods.add(new BlockingHit());
        mods.add(new NoSlow());
        mods.add(new Eagle());
        mods.add(new AutoClicker());
        mods.add(new AutoTool());
        mods.add(new AimAssist());
    }
}
