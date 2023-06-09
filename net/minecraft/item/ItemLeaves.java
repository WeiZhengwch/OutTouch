package net.minecraft.item;

import net.minecraft.block.BlockLeaves;

public class ItemLeaves extends ItemBlock {
    private final BlockLeaves leaves;

    public ItemLeaves(BlockLeaves block) {
        super(block);
        leaves = block;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage) {
        return damage | 4;
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return leaves.getRenderColor(leaves.getStateFromMeta(stack.getMetadata()));
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + leaves.getWoodType(stack.getMetadata()).getUnlocalizedName();
    }
}
