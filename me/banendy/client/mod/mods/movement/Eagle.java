package me.banendy.client.mod.mods.movement;

import me.banendy.client.mod.Mod;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class Eagle extends Mod {
    public static boolean isEagling;
    public Eagle() {
        super("Eagle", true);
        setKey(Keyboard.KEY_G);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public void update() {
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir) {
            if (mc.gameSettings.keyBindForward.pressed && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                isEagling = false;
            } else if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock && !mc.gameSettings.keyBindJump.pressed && mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                isEagling = true;
            }
        } else if (isEagling && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            isEagling = false;
        }
//        if (mc.thePlayer.onGround && mc.thePlayer.movementInput.moveForward != 0) {
//            mc.thePlayer.jump();
//        }
//        if (mc.thePlayer.motionY <= 0 && mc.thePlayer.fallDistance <= 2) {
//            mc.thePlayer.motionY *= 1.15;
//            mc.thePlayer.speedInAir = 0.0225F;
//            mc.thePlayer.motionX *= 1.001;
//            mc.thePlayer.motionZ *= 1.001;
//        } else if (mc.thePlayer.motionY < 0.40 && !(mc.thePlayer.motionY <= 0)) {
//            mc.thePlayer.speedInAir = 0.02F;
//        }
    }
}
