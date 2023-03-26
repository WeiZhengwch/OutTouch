package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class BlockOldLog extends BlockLog {
    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class, p_apply_1_ -> p_apply_1_.getMetadata() < 4);

    public BlockOldLog() {
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK).withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state) {
        BlockPlanks.EnumType blockplanks$enumtype = state.getValue(VARIANT);

        return switch (state.getValue(LOG_AXIS)) {
            case Y -> blockplanks$enumtype.getMapColor();
            default -> switch (blockplanks$enumtype) {
                case SPRUCE -> BlockPlanks.EnumType.DARK_OAK.getMapColor();
                case BIRCH -> MapColor.quartzColor;
                default -> BlockPlanks.EnumType.SPRUCE.getMapColor();
            };
        };
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.OAK.getMetadata()));
        list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
        list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
        list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata((meta & 3) % 4));
        iblockstate = switch (meta & 12) {
            case 0 -> iblockstate.withProperty(LOG_AXIS, EnumAxis.Y);
            case 4 -> iblockstate.withProperty(LOG_AXIS, EnumAxis.X);
            case 8 -> iblockstate.withProperty(LOG_AXIS, EnumAxis.Z);
            default -> iblockstate.withProperty(LOG_AXIS, EnumAxis.NONE);
        };

        return iblockstate;
    }

    @SuppressWarnings("incomplete-switch")

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(VARIANT).getMetadata();

        switch (state.getValue(LOG_AXIS)) {
            case X -> i |= 4;
            case Z -> i |= 8;
            case NONE -> i |= 12;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT, LOG_AXIS);
    }

    protected ItemStack createStackedBlock(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
}
