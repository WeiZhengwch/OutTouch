package me.banendy.client.mod.mods.player;

import me.banendy.client.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class AutoTool extends Mod {
    BlockPos lastBlockPos;
    BlockPos nowBlockPos;

    public AutoTool() {
        super("AutoTool", mc.gameSettings.autotool);
        setKey(Keyboard.KEY_B);
    }

    public boolean isEnable() {
        return mc.gameSettings.autotool;
    }

    public void update() {
        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            nowBlockPos = mc.objectMouseOver != null ? mc.objectMouseOver.getBlockPos() : null;
            if (((mc.thePlayer.getHeldItem() != null && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) || mc.thePlayer.getHeldItem() == null) && nowBlockPos != null && !nowBlockPos.equals(lastBlockPos)) {
                float f = 1.0f;
                int n = -1;
                for (int i = 0; i < 9; ++i) {
                    ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (itemStack == null || !(itemStack.getStrVsBlock(mc.theWorld.getBlockState(nowBlockPos).getBlock()) > f))
                        continue;
                    n = i;
                    f = itemStack.getStrVsBlock(mc.theWorld.getBlockState(nowBlockPos).getBlock());
                }
                if (n != -1 && mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) != mc.thePlayer.inventory.getStackInSlot(n)) {
                    mc.thePlayer.inventory.currentItem = n;
                }
                lastBlockPos = nowBlockPos;
            }
        } else if (nowBlockPos != null && nowBlockPos.equals(lastBlockPos)) {
            lastBlockPos = new BlockPos(0, -1, 0);
        }
    }
}
