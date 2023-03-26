package me.banendy.client.mod.mods.render;

import me.banendy.client.MainClient;
import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IntHashMap;

import java.util.List;

public class ModulesList extends Mod {
    private static final IntHashMap hash = new IntHashMap();

    public ModulesList() {
        super("ModulesList", true);
    }

    public boolean isEnable() {
        return mc.gameSettings.moduleslist;
    }

    @Override
    public void render() {
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        List<Mod> enableMods = MainClient.ModManager.getEnableMods();
        enableMods.sort(((o1, o2) -> font.getStringWidth(o2.getName()) - font.getStringWidth(o1.getName())));

        int y = 0;
        for (Mod enableMod : enableMods) {
            font.drawString(enableMod.getName(), sr.getScaledWidth() - font.getStringWidth(enableMod.getName()) - 1, 2 + y, 0xFFFFFFFF);
            y += font.FONT_HEIGHT + 1;
        }
    }
}
