package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class InventoryLargeChest implements ILockableContainer {
    /**
     * Name of the chest.
     */
    private final String name;

    /**
     * Inventory object corresponding to double chest upper part
     */
    private final ILockableContainer upperChest;

    /**
     * Inventory object corresponding to double chest lower part
     */
    private final ILockableContainer lowerChest;

    public InventoryLargeChest(String nameIn, ILockableContainer upperChestIn, ILockableContainer lowerChestIn) {
        name = nameIn;

        if (upperChestIn == null) {
            upperChestIn = lowerChestIn;
        }

        if (lowerChestIn == null) {
            lowerChestIn = upperChestIn;
        }

        upperChest = upperChestIn;
        lowerChest = lowerChestIn;

        if (upperChestIn.isLocked()) {
            lowerChestIn.setLockCode(upperChestIn.getLockCode());
        } else if (lowerChestIn.isLocked()) {
            upperChestIn.setLockCode(lowerChestIn.getLockCode());
        }
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return upperChest.getSizeInventory() + lowerChest.getSizeInventory();
    }

    /**
     * Return whether the given inventory is part of this large chest.
     */
    public boolean isPartOfLargeChest(IInventory inventoryIn) {
        return upperChest == inventoryIn || lowerChest == inventoryIn;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return upperChest.hasCustomName() ? upperChest.getName() : (lowerChest.hasCustomName() ? lowerChest.getName() : name);
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName() {
        return upperChest.hasCustomName() || lowerChest.hasCustomName();
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index) {
        return index >= upperChest.getSizeInventory() ? lowerChest.getStackInSlot(index - upperChest.getSizeInventory()) : upperChest.getStackInSlot(index);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count) {
        return index >= upperChest.getSizeInventory() ? lowerChest.decrStackSize(index - upperChest.getSizeInventory(), count) : upperChest.decrStackSize(index, count);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index) {
        return index >= upperChest.getSizeInventory() ? lowerChest.removeStackFromSlot(index - upperChest.getSizeInventory()) : upperChest.removeStackFromSlot(index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= upperChest.getSizeInventory()) {
            lowerChest.setInventorySlotContents(index - upperChest.getSizeInventory(), stack);
        } else {
            upperChest.setInventorySlotContents(index, stack);
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit() {
        return upperChest.getInventoryStackLimit();
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
        upperChest.markDirty();
        lowerChest.markDirty();
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player) {
        return upperChest.isUseableByPlayer(player) && lowerChest.isUseableByPlayer(player);
    }

    public void openInventory(EntityPlayer player) {
        upperChest.openInventory(player);
        lowerChest.openInventory(player);
    }

    public void closeInventory(EntityPlayer player) {
        upperChest.closeInventory(player);
        lowerChest.closeInventory(player);
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {
    }

    public int getFieldCount() {
        return 0;
    }

    public boolean isLocked() {
        return upperChest.isLocked() || lowerChest.isLocked();
    }

    public LockCode getLockCode() {
        return upperChest.getLockCode();
    }

    public void setLockCode(LockCode code) {
        upperChest.setLockCode(code);
        lowerChest.setLockCode(code);
    }

    public String getGuiID() {
        return upperChest.getGuiID();
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    public void clear() {
        upperChest.clear();
        lowerChest.clear();
    }
}
