package net.minecraft.entity.player;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;

public class InventoryPlayer implements IInventory {
    /**
     * An array of 36 item stacks indicating the main player inventory (including the visible bar).
     */
    public ItemStack[] mainInventory = new ItemStack[36];

    /**
     * An array of 4 item stacks containing the currently worn armor pieces.
     */
    public ItemStack[] armorInventory = new ItemStack[4];

    /**
     * The index of the currently held item (0-8).
     */
    public int currentItem;

    /**
     * The player whose inventory this is.
     */
    public EntityPlayer player;
    /**
     * Set true whenever the inventory changes. Nothing sets it false so you will have to write your own code to check
     * it and reset the value.
     */
    public boolean inventoryChanged;
    private ItemStack itemStack;

    public InventoryPlayer(EntityPlayer playerIn) {
        player = playerIn;
    }

    /**
     * Get the size of the player hotbar inventory
     */
    public static int getHotbarSize() {
        return 9;
    }

    /**
     * Returns the item stack currently held by the player.
     */
    public ItemStack getCurrentItem() {
        return currentItem < 9 && currentItem >= 0 ? mainInventory[currentItem] : null;
    }

    private int getInventorySlotContainItem(Item itemIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemIn) {
                return i;
            }
        }

        return -1;
    }

    private int getInventorySlotContainItemAndDamage(Item itemIn, int metadataIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemIn && mainInventory[i].getMetadata() == metadataIn) {
                return i;
            }
        }

        return -1;
    }

    /**
     * stores an itemstack in the users inventory
     */
    private int storeItemStack(ItemStack itemStackIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemStackIn.getItem() && mainInventory[i].isStackable() && mainInventory[i].stackSize < mainInventory[i].getMaxStackSize() && mainInventory[i].stackSize < getInventoryStackLimit() && (!mainInventory[i].getHasSubtypes() || mainInventory[i].getMetadata() == itemStackIn.getMetadata()) && ItemStack.areItemStackTagsEqual(mainInventory[i], itemStackIn)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the first item stack that is empty.
     */
    public int getFirstEmptyStack() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public void setCurrentItem(Item itemIn, int metadataIn, boolean isMetaSpecific, boolean p_146030_4_) {
        ItemStack itemstack = getCurrentItem();
        int i = isMetaSpecific ? getInventorySlotContainItemAndDamage(itemIn, metadataIn) : getInventorySlotContainItem(itemIn);

        if (i >= 0 && i < 9) {
            currentItem = i;
        } else if (p_146030_4_ && itemIn != null) {
            int j = getFirstEmptyStack();

            if (j >= 0 && j < 9) {
                currentItem = j;
            }

            if (itemstack == null || !itemstack.isItemEnchantable() || getInventorySlotContainItemAndDamage(itemstack.getItem(), itemstack.getItemDamage()) != currentItem) {
                int k = getInventorySlotContainItemAndDamage(itemIn, metadataIn);
                int l;

                if (k >= 0) {
                    l = mainInventory[k].stackSize;
                    mainInventory[k] = mainInventory[currentItem];
                } else {
                    l = 1;
                }

                mainInventory[currentItem] = new ItemStack(itemIn, l, metadataIn);
            }
        }
    }

    /**
     * Switch the current item to the next one or the previous one
     *
     * @param direction Direction to switch (1, 0, -1). 1 (any > 0) to select item left of current (decreasing
     *                  currentItem index), -1 (any < 0) to select item right of current (increasing currentItem index). 0 has no effect.
     */
    public void changeCurrentItem(int direction) {
        if (direction > 0) {
            direction = 1;
        }

        if (direction < 0) {
            direction = -1;
        }

        for (currentItem -= direction; currentItem < 0; currentItem += 9) {
        }

        while (currentItem >= 9) {
            currentItem -= 9;
        }
    }

    /**
     * Removes matching items from the inventory.
     *
     * @param itemIn      The item to match, null ignores.
     * @param metadataIn  The metadata to match, -1 ignores.
     * @param removeCount The number of items to remove. If less than 1, removes all matching items.
     * @param itemNBT     The NBT data to match, null ignores.
     * @return The number of items removed from the inventory.
     */
    public int clearMatchingItems(Item itemIn, int metadataIn, int removeCount, NBTTagCompound itemNBT) {
        int i = 0;

        for (int j = 0; j < mainInventory.length; ++j) {
            ItemStack itemstack = mainInventory[j];

            if (itemstack != null && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack.getTagCompound(), true))) {
                int k = removeCount <= 0 ? itemstack.stackSize : Math.min(removeCount - i, itemstack.stackSize);
                i += k;

                if (removeCount != 0) {
                    mainInventory[j].stackSize -= k;

                    if (mainInventory[j].stackSize == 0) {
                        mainInventory[j] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        for (int l = 0; l < armorInventory.length; ++l) {
            ItemStack itemstack1 = armorInventory[l];

            if (itemstack1 != null && (itemIn == null || itemstack1.getItem() == itemIn) && (metadataIn <= -1 || itemstack1.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack1.getTagCompound(), false))) {
                int j1 = removeCount <= 0 ? itemstack1.stackSize : Math.min(removeCount - i, itemstack1.stackSize);
                i += j1;

                if (removeCount != 0) {
                    armorInventory[l].stackSize -= j1;

                    if (armorInventory[l].stackSize == 0) {
                        armorInventory[l] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        if (itemStack != null) {
            if (itemIn != null && itemStack.getItem() != itemIn) {
                return i;
            }

            if (metadataIn > -1 && itemStack.getMetadata() != metadataIn) {
                return i;
            }

            if (itemNBT != null && !NBTUtil.func_181123_a(itemNBT, itemStack.getTagCompound(), false)) {
                return i;
            }

            int i1 = removeCount <= 0 ? itemStack.stackSize : Math.min(removeCount - i, itemStack.stackSize);
            i += i1;

            if (removeCount != 0) {
                itemStack.stackSize -= i1;

                if (itemStack.stackSize == 0) {
                    itemStack = null;
                }

                if (removeCount > 0 && i >= removeCount) {
                    return i;
                }
            }
        }

        return i;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private int storePartialItemStack(ItemStack itemStackIn) {
        Item item = itemStackIn.getItem();
        int i = itemStackIn.stackSize;
        int j = storeItemStack(itemStackIn);

        if (j < 0) {
            j = getFirstEmptyStack();
        }

        if (j < 0) {
            return i;
        } else {
            if (mainInventory[j] == null) {
                mainInventory[j] = new ItemStack(item, 0, itemStackIn.getMetadata());

                if (itemStackIn.hasTagCompound()) {
                    mainInventory[j].setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
                }
            }

            int k = i;

            if (i > mainInventory[j].getMaxStackSize() - mainInventory[j].stackSize) {
                k = mainInventory[j].getMaxStackSize() - mainInventory[j].stackSize;
            }

            if (k > getInventoryStackLimit() - mainInventory[j].stackSize) {
                k = getInventoryStackLimit() - mainInventory[j].stackSize;
            }

            if (k == 0) {
                return i;
            } else {
                i = i - k;
                mainInventory[j].stackSize += k;
                mainInventory[j].animationsToGo = 5;
                return i;
            }
        }
    }

    /**
     * Decrement the number of animations remaining. Only called on client side. This is used to handle the animation of
     * receiving a block.
     */
    public void decrementAnimations() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                mainInventory[i].updateAnimation(player.worldObj, player, i, currentItem == i);
            }
        }
    }

    /**
     * removed one item of specified Item from inventory (if it is in a stack, the stack size will reduce with 1)
     */
    public boolean consumeInventoryItem(Item itemIn) {
        int i = getInventorySlotContainItem(itemIn);

        if (i < 0) {
            return false;
        } else {
            if (--mainInventory[i].stackSize <= 0) {
                mainInventory[i] = null;
            }

            return true;
        }
    }

    /**
     * Checks if a specified Item is inside the inventory
     */
    public boolean hasItem(Item itemIn) {
        int i = getInventorySlotContainItem(itemIn);
        return i >= 0;
    }

    /**
     * Adds the item stack to the inventory, returns false if it is impossible.
     */
    public boolean addItemStackToInventory(final ItemStack itemStackIn) {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
            try {
                if (itemStackIn.isItemDamaged()) {
                    int j = getFirstEmptyStack();

                    if (j >= 0) {
                        mainInventory[j] = ItemStack.copyItemStack(itemStackIn);
                        mainInventory[j].animationsToGo = 5;
                        itemStackIn.stackSize = 0;
                        return true;
                    } else if (player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;

                    while (true) {
                        i = itemStackIn.stackSize;
                        itemStackIn.stackSize = storePartialItemStack(itemStackIn);

                        if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i) {
                            break;
                        }
                    }

                    if (itemStackIn.stackSize == i && player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return itemStackIn.stackSize < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(itemStackIn.getItem()));
                crashreportcategory.addCrashSection("Item data", itemStackIn.getMetadata());
                crashreportcategory.addCrashSectionCallable("Item name", () -> itemStackIn.getDisplayName());
                throw new ReportedException(crashreport);
            }
        } else {
            return false;
        }
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= mainInventory.length) {
            aitemstack = armorInventory;
            index -= mainInventory.length;
        }

        if (aitemstack[index] != null) {
            if (aitemstack[index].stackSize <= count) {
                ItemStack itemstack1 = aitemstack[index];
                aitemstack[index] = null;
                return itemstack1;
            } else {
                ItemStack itemstack = aitemstack[index].splitStack(count);

                if (aitemstack[index].stackSize == 0) {
                    aitemstack[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= mainInventory.length) {
            aitemstack = armorInventory;
            index -= mainInventory.length;
        }

        if (aitemstack[index] != null) {
            ItemStack itemstack = aitemstack[index];
            aitemstack[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = armorInventory;
        }

        aitemstack[index] = stack;
    }

    public float getStrVsBlock(Block blockIn) {
        float f = 1.0F;

        if (mainInventory[currentItem] != null) {
            f *= mainInventory[currentItem].getStrVsBlock(blockIn);
        }

        return f;
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     *
     * @param nbtTagListIn List to append tags to
     */
    public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                mainInventory[i].writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            if (armorInventory[j] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) (j + 100));
                armorInventory[j].writeToNBT(nbttagcompound1);
                nbtTagListIn.appendTag(nbttagcompound1);
            }
        }

        return nbtTagListIn;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     *
     * @param nbtTagListIn tagList to read from
     */
    public void readFromNBT(NBTTagList nbtTagListIn) {
        mainInventory = new ItemStack[36];
        armorInventory = new ItemStack[4];

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null) {
                if (j >= 0 && j < mainInventory.length) {
                    mainInventory[j] = itemstack;
                }

                if (j >= 100 && j < armorInventory.length + 100) {
                    armorInventory[j - 100] = itemstack;
                }
            }
        }
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return mainInventory.length + 4;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index) {
        ItemStack[] aitemstack = mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = armorInventory;
        }

        return aitemstack[index];
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return "container.inventory";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName() {
        return false;
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

    public boolean canHeldItemHarvest(Block blockIn) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            ItemStack itemstack = getStackInSlot(currentItem);
            return itemstack != null && itemstack.canHarvestBlock(blockIn);
        }
    }

    /**
     * returns a player armor item (as itemstack) contained in specified armor slot.
     *
     * @param slotIn the slot index requested
     */
    public ItemStack armorItemInSlot(int slotIn) {
        return armorInventory[slotIn];
    }

    /**
     * Based on the damage values and maximum damage values of each armor item, returns the current armor value.
     */
    public int getTotalArmorValue() {
        int i = 0;

        for (ItemStack stack : armorInventory) {
            if (stack != null && stack.getItem() instanceof ItemArmor) {
                int k = ((ItemArmor) stack.getItem()).damageReduceAmount;
                i += k;
            }
        }

        return i;
    }

    /**
     * Damages armor in each slot by the specified amount.
     */
    public void damageArmor(float damage) {
        damage = damage / 4.0F;

        if (damage < 1.0F) {
            damage = 1.0F;
        }

        for (int i = 0; i < armorInventory.length; ++i) {
            if (armorInventory[i] != null && armorInventory[i].getItem() instanceof ItemArmor) {
                armorInventory[i].damageItem((int) damage, player);

                if (armorInventory[i].stackSize == 0) {
                    armorInventory[i] = null;
                }
            }
        }
    }

    /**
     * Drop all armor and main inventory items.
     */
    public void dropAllItems() {
        for (int i = 0; i < mainInventory.length; ++i) {
            if (mainInventory[i] != null) {
                player.dropItem(mainInventory[i], true, false);
                mainInventory[i] = null;
            }
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            if (armorInventory[j] != null) {
                player.dropItem(armorInventory[j], true, false);
                armorInventory[j] = null;
            }
        }
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
        inventoryChanged = true;
    }

    /**
     * Stack helds by mouse, used in GUI and Containers
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Set the stack helds by mouse, used in GUI/Container
     */
    public void setItemStack(ItemStack itemStackIn) {
        itemStack = itemStackIn;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player) {
        return !this.player.isDead && player.getDistanceSqToEntity(this.player) <= 64.0D;
    }

    /**
     * Returns true if the specified ItemStack exists in the inventory.
     */
    public boolean hasItemStack(ItemStack itemStackIn) {
        for (ItemStack stack : armorInventory) {
            if (stack != null && stack.isItemEqual(itemStackIn)) {
                return true;
            }
        }

        for (ItemStack stack : mainInventory) {
            if (stack != null && stack.isItemEqual(itemStackIn)) {
                return true;
            }
        }

        return false;
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

    /**
     * Copy the ItemStack contents from another InventoryPlayer instance
     */
    public void copyInventory(InventoryPlayer playerInventory) {
        for (int i = 0; i < mainInventory.length; ++i) {
            mainInventory[i] = ItemStack.copyItemStack(playerInventory.mainInventory[i]);
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            armorInventory[j] = ItemStack.copyItemStack(playerInventory.armorInventory[j]);
        }

        currentItem = playerInventory.currentItem;
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
        for (int i = 0; i < mainInventory.length; ++i) {
            mainInventory[i] = null;
        }

        for (int j = 0; j < armorInventory.length; ++j) {
            armorInventory[j] = null;
        }
    }
}
