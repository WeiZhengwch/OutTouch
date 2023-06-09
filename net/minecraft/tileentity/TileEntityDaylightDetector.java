package net.minecraft.tileentity;

import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.util.ITickable;

public class TileEntityDaylightDetector extends TileEntity implements ITickable {
    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        if (worldObj != null && !worldObj.isRemote && worldObj.getTotalWorldTime() % 20L == 0L) {
            blockType = getBlockType();

            if (blockType instanceof BlockDaylightDetector) {
                ((BlockDaylightDetector) blockType).updatePower(worldObj, pos);
            }
        }
    }
}
