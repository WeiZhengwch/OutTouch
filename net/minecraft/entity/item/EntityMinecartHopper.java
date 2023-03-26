package net.minecraft.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

import java.util.List;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper {
    private final BlockPos field_174900_c = BlockPos.ORIGIN;
    /**
     * Whether this hopper minecart is being blocked by an activator rail.
     */
    private boolean isBlocked = true;
    private int transferTicker = -1;

    public EntityMinecartHopper(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartHopper(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public EntityMinecart.EnumMinecartType getMinecartType() {
        return EntityMinecart.EnumMinecartType.HOPPER;
    }

    public IBlockState getDefaultDisplayTile() {
        return Blocks.hopper.getDefaultState();
    }

    public int getDefaultDisplayTileOffset() {
        return 1;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return 5;
    }

    /**
     * First layer of player interaction
     */
    public boolean interactFirst(EntityPlayer playerIn) {
        if (!worldObj.isRemote) {
            playerIn.displayGUIChest(this);
        }

        return true;
    }

    /**
     * Called every tick the minecart is on an activator rail. Args: x, y, z, is the rail receiving power
     */
    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        boolean flag = !receivingPower;

        if (flag != getBlocked()) {
            setBlocked(flag);
        }
    }

    /**
     * Get whether this hopper minecart is being blocked by an activator rail.
     */
    public boolean getBlocked() {
        return isBlocked;
    }

    /**
     * Set whether this hopper minecart is being blocked by an activator rail.
     */
    public void setBlocked(boolean p_96110_1_) {
        isBlocked = p_96110_1_;
    }

    /**
     * Returns the worldObj for this tileEntity.
     */
    public World getWorld() {
        return worldObj;
    }

    /**
     * Gets the world X position for this hopper entity.
     */
    public double getXPos() {
        return posX;
    }

    /**
     * Gets the world Y position for this hopper entity.
     */
    public double getYPos() {
        return posY + 0.5D;
    }

    /**
     * Gets the world Z position for this hopper entity.
     */
    public double getZPos() {
        return posZ;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote && isEntityAlive() && getBlocked()) {
            BlockPos blockpos = new BlockPos(this);

            if (blockpos.equals(field_174900_c)) {
                --transferTicker;
            } else {
                setTransferTicker(0);
            }

            if (!canTransfer()) {
                setTransferTicker(0);

                if (func_96112_aD()) {
                    setTransferTicker(4);
                    markDirty();
                }
            }
        }
    }

    public boolean func_96112_aD() {
        if (TileEntityHopper.captureDroppedItems(this)) {
            return true;
        } else {
            List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(0.25D, 0.0D, 0.25D), EntitySelectors.selectAnything);

            if (list.size() > 0) {
                TileEntityHopper.putDropInInventoryAllSlots(this, list.get(0));
            }

            return false;
        }
    }

    public void killMinecart(DamageSource source) {
        super.killMinecart(source);

        if (worldObj.getGameRules().getBoolean("doEntityDrops")) {
            dropItemWithOffset(Item.getItemFromBlock(Blocks.hopper), 1, 0.0F);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("TransferCooldown", transferTicker);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        transferTicker = tagCompund.getInteger("TransferCooldown");
    }

    /**
     * Sets the transfer ticker, used to determine the delay between transfers.
     */
    public void setTransferTicker(int p_98042_1_) {
        transferTicker = p_98042_1_;
    }

    /**
     * Returns whether the hopper cart can currently transfer an item.
     */
    public boolean canTransfer() {
        return transferTicker > 0;
    }

    public String getGuiID() {
        return "minecraft:hopper";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerHopper(playerInventory, this, playerIn);
    }
}
