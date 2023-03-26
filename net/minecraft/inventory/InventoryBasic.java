package net.minecraft.inventory;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class InventoryBasic implements IInventory {
    private final int slotsCount;
    private final ItemStack[] inventoryContents;
    private String inventoryTitle;
    private List<IInvBasic> changeListeners;
    private boolean hasCustomName;

    public InventoryBasic(String title, boolean customName, int slotCount) {
        inventoryTitle = title;
        hasCustomName = customName;
        slotsCount = slotCount;
        inventoryContents = new ItemStack[slotCount];
    }

    public InventoryBasic(IChatComponent title, int slotCount) {
        this(title.getUnformattedText(), true, slotCount);
    }

    /**
     * Add a listener that will be notified when any item in this inventory is modified.
     *
     * @param listener the listener to add
     */
    public void addInventoryChangeListener(IInvBasic listener) {
        if (changeListeners == null) {
            changeListeners = Lists.newArrayList();
        }

        changeListeners.add(listener);
    }

    /**
     * removes the specified IInvBasic from receiving further change notices
     *
     * @param listener the listener to remove
     */
    public void removeInventoryChangeListener(IInvBasic listener) {
        changeListeners.remove(listener);
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < inventoryContents.length ? inventoryContents[index] : null;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count) {
        if (inventoryContents[index] != null) {
            if (inventoryContents[index].stackSize <= count) {
                ItemStack itemstack1 = inventoryContents[index];
                inventoryContents[index] = null;
                markDirty();
                return itemstack1;
            } else {
                ItemStack itemstack = inventoryContents[index].splitStack(count);

                if (inventoryContents[index].stackSize == 0) {
                    inventoryContents[index] = null;
                }

                markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack func_174894_a(ItemStack stack) {
        ItemStack itemstack = stack.copy();

        for (int i = 0; i < slotsCount; ++i) {
            ItemStack itemstack1 = getStackInSlot(i);

            if (itemstack1 == null) {
                setInventorySlotContents(i, itemstack);
                markDirty();
                return null;
            }

            if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
                int j = Math.min(getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.stackSize, j - itemstack1.stackSize);

                if (k > 0) {
                    itemstack1.stackSize += k;
                    itemstack.stackSize -= k;

                    if (itemstack.stackSize <= 0) {
                        markDirty();
                        return null;
                    }
                }
            }
        }

        if (itemstack.stackSize != stack.stackSize) {
            markDirty();
        }

        return itemstack;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index) {
        if (inventoryContents[index] != null) {
            ItemStack itemstack = inventoryContents[index];
            inventoryContents[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryContents[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return slotsCount;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return inventoryTitle;
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName() {
        return hasCustomName;
    }

    /**
     * Sets the name of this inventory. This is displayed to the client on opening.
     */
    public void setCustomName(String inventoryTitleIn) {
        hasCustomName = true;
        inventoryTitle = inventoryTitleIn;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
        if (changeListeners != null) {
            for (IInvBasic changeListener : changeListeners) {
                changeListener.onInventoryChanged(this);
            }
        }
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
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

    public void clear() {
        for (int i = 0; i < inventoryContents.length; ++i) {
            inventoryContents[i] = null;
        }
    }
}
