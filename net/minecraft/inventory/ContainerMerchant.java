package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerMerchant extends Container {
    /**
     * Instance of Merchant.
     */
    private final IMerchant theMerchant;
    private final InventoryMerchant merchantInventory;

    /**
     * Instance of World.
     */
    private final World theWorld;

    public ContainerMerchant(InventoryPlayer playerInventory, IMerchant merchant, World worldIn) {
        theMerchant = merchant;
        theWorld = worldIn;
        merchantInventory = new InventoryMerchant(playerInventory.player, merchant);
        addSlotToContainer(new Slot(merchantInventory, 0, 36, 53));
        addSlotToContainer(new Slot(merchantInventory, 1, 62, 53));
        addSlotToContainer(new SlotMerchantResult(playerInventory.player, merchant, merchantInventory, 2, 120, 53));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public InventoryMerchant getMerchantInventory() {
        return merchantInventory;
    }

    public void onCraftGuiOpened(ICrafting listener) {
        super.onCraftGuiOpened(listener);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        merchantInventory.resetRecipeAndSlots();
        super.onCraftMatrixChanged(inventoryIn);
    }

    public void setCurrentRecipeIndex(int currentRecipeIndex) {
        merchantInventory.setCurrentRecipeIndex(currentRecipeIndex);
    }

    public void updateProgressBar(int id, int data) {
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return theMerchant.getCustomer() == playerIn;
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 2) {
                if (!mergeItemStack(itemstack1, 3, 39, true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!mergeItemStack(itemstack1, 30, 39, false)) {
                        return null;
                    }
                } else if (index >= 30 && index < 39 && !mergeItemStack(itemstack1, 3, 30, false)) {
                    return null;
                }
            } else if (!mergeItemStack(itemstack1, 3, 39, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        theMerchant.setCustomer(null);
        super.onContainerClosed(playerIn);

        if (!theWorld.isRemote) {
            ItemStack itemstack = merchantInventory.removeStackFromSlot(0);

            if (itemstack != null) {
                playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
            }

            itemstack = merchantInventory.removeStackFromSlot(1);

            if (itemstack != null) {
                playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
            }
        }
    }
}
