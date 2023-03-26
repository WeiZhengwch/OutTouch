package net.minecraft.village;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

public class MerchantRecipeList extends ArrayList<MerchantRecipe> {
    public MerchantRecipeList() {
    }

    public MerchantRecipeList(NBTTagCompound compound) {
        readRecipiesFromTags(compound);
    }

    public static MerchantRecipeList readFromBuf(PacketBuffer buffer) throws IOException {
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        int i = buffer.readByte() & 255;

        for (int j = 0; j < i; ++j) {
            ItemStack itemstack = buffer.readItemStackFromBuffer();
            ItemStack itemstack1 = buffer.readItemStackFromBuffer();
            ItemStack itemstack2 = null;

            if (buffer.readBoolean()) {
                itemstack2 = buffer.readItemStackFromBuffer();
            }

            boolean flag = buffer.readBoolean();
            int k = buffer.readInt();
            int l = buffer.readInt();
            MerchantRecipe merchantrecipe = new MerchantRecipe(itemstack, itemstack2, itemstack1, k, l);

            if (flag) {
                merchantrecipe.compensateToolUses();
            }

            merchantrecipelist.add(merchantrecipe);
        }

        return merchantrecipelist;
    }

    /**
     * can par1,par2 be used to in crafting recipe par3
     */
    public MerchantRecipe canRecipeBeUsed(ItemStack p_77203_1_, ItemStack p_77203_2_, int p_77203_3_) {
        if (p_77203_3_ > 0 && p_77203_3_ < size()) {
            MerchantRecipe merchantrecipe1 = get(p_77203_3_);
            return !func_181078_a(p_77203_1_, merchantrecipe1.getItemToBuy()) || (p_77203_2_ != null || merchantrecipe1.hasSecondItemToBuy()) && (!merchantrecipe1.hasSecondItemToBuy() || !func_181078_a(p_77203_2_, merchantrecipe1.getSecondItemToBuy())) || p_77203_1_.stackSize < merchantrecipe1.getItemToBuy().stackSize || merchantrecipe1.hasSecondItemToBuy() && p_77203_2_.stackSize < merchantrecipe1.getSecondItemToBuy().stackSize ? null : merchantrecipe1;
        } else {
            for (int i = 0; i < size(); ++i) {
                MerchantRecipe merchantrecipe = get(i);

                if (func_181078_a(p_77203_1_, merchantrecipe.getItemToBuy()) && p_77203_1_.stackSize >= merchantrecipe.getItemToBuy().stackSize && (!merchantrecipe.hasSecondItemToBuy() && p_77203_2_ == null || merchantrecipe.hasSecondItemToBuy() && func_181078_a(p_77203_2_, merchantrecipe.getSecondItemToBuy()) && p_77203_2_.stackSize >= merchantrecipe.getSecondItemToBuy().stackSize)) {
                    return merchantrecipe;
                }
            }

            return null;
        }
    }

    private boolean func_181078_a(ItemStack p_181078_1_, ItemStack p_181078_2_) {
        return ItemStack.areItemsEqual(p_181078_1_, p_181078_2_) && (!p_181078_2_.hasTagCompound() || p_181078_1_.hasTagCompound() && NBTUtil.func_181123_a(p_181078_2_.getTagCompound(), p_181078_1_.getTagCompound(), false));
    }

    public void writeToBuf(PacketBuffer buffer) {
        buffer.writeByte((byte) (size() & 255));

        for (MerchantRecipe merchantrecipe : this) {
            buffer.writeItemStackToBuffer(merchantrecipe.getItemToBuy());
            buffer.writeItemStackToBuffer(merchantrecipe.getItemToSell());
            ItemStack itemstack = merchantrecipe.getSecondItemToBuy();
            buffer.writeBoolean(itemstack != null);

            if (itemstack != null) {
                buffer.writeItemStackToBuffer(itemstack);
            }

            buffer.writeBoolean(merchantrecipe.isRecipeDisabled());
            buffer.writeInt(merchantrecipe.getToolUses());
            buffer.writeInt(merchantrecipe.getMaxTradeUses());
        }
    }

    public void readRecipiesFromTags(NBTTagCompound compound) {
        NBTTagList nbttaglist = compound.getTagList("Recipes", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            add(new MerchantRecipe(nbttagcompound));
        }
    }

    public NBTTagCompound getRecipiesAsTags() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();

        for (MerchantRecipe merchantrecipe : this) {
            nbttaglist.appendTag(merchantrecipe.writeToTags());
        }

        nbttagcompound.setTag("Recipes", nbttaglist);
        return nbttagcompound;
    }
}
