package me.banendy.client.mod.mods.movement;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

public class Sprint extends Mod {
    public Sprint() {
        super("Sprint", true);
        setKey(Keyboard.KEY_V);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public void update() {
        if (mc.gameSettings.keyBindForward.isKeyDown() && (mc.gameSettings.noslow || mc.thePlayer.getItemInUse() == null) && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isPotionActive(Potion.blindness) && (mc.thePlayer.capabilities.allowFlying || mc.thePlayer.getFoodStats().getFoodLevel() > 6)) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
