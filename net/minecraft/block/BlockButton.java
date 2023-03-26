package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public abstract class BlockButton extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    private final boolean wooden;

    protected BlockButton(boolean wooden) {
        super(Material.circuits);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.FALSE));
        setTickRandomly(true);
        setCreativeTab(CreativeTabs.tabRedstone);
        this.wooden = wooden;
    }

    protected static boolean func_181088_a(World p_181088_0_, BlockPos p_181088_1_, EnumFacing p_181088_2_) {
        BlockPos blockpos = p_181088_1_.offset(p_181088_2_);
        return p_181088_2_ == EnumFacing.DOWN ? World.doesBlockHaveSolidTopSurface(p_181088_0_, blockpos) : p_181088_0_.getBlockState(blockpos).getBlock().isNormalCube();
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn) {
        return wooden ? 30 : 20;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return func_181088_a(worldIn, pos, side.getOpposite());
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (func_181088_a(worldIn, pos, enumfacing)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return func_181088_a(worldIn, pos, facing.getOpposite()) ? getDefaultState().withProperty(FACING, facing).withProperty(POWERED, Boolean.FALSE) : getDefaultState().withProperty(FACING, EnumFacing.DOWN).withProperty(POWERED, Boolean.FALSE);
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (checkForDrop(worldIn, pos, state) && !func_181088_a(worldIn, pos, state.getValue(FACING).getOpposite())) {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
        if (canPlaceBlockAt(worldIn, pos)) {
            return true;
        } else {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
            return false;
        }
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        updateBlockBounds(worldIn.getBlockState(pos));
    }

    private void updateBlockBounds(IBlockState state) {
        EnumFacing enumfacing = state.getValue(FACING);
        boolean flag = state.getValue(POWERED);
        float f = 0.25F;
        float f1 = 0.375F;
        float f2 = (float) (flag ? 1 : 2) / 16.0F;
        float f3 = 0.125F;
        float f4 = 0.1875F;

        switch (enumfacing) {
            case EAST -> setBlockBounds(0.0F, 0.375F, 0.3125F, f2, 0.625F, 0.6875F);
            case WEST -> setBlockBounds(1.0F - f2, 0.375F, 0.3125F, 1.0F, 0.625F, 0.6875F);
            case SOUTH -> setBlockBounds(0.3125F, 0.375F, 0.0F, 0.6875F, 0.625F, f2);
            case NORTH -> setBlockBounds(0.3125F, 0.375F, 1.0F - f2, 0.6875F, 0.625F, 1.0F);
            case UP -> setBlockBounds(0.3125F, 0.0F, 0.375F, 0.6875F, 0.0F + f2, 0.625F);
            case DOWN -> setBlockBounds(0.3125F, 1.0F - f2, 0.375F, 0.6875F, 1.0F, 0.625F);
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!state.getValue(POWERED)) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.TRUE), 3);
            worldIn.markBlockRangeForRenderUpdate(pos, pos);
            worldIn.playSoundEffect((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
            notifyNeighbors(worldIn, pos, state.getValue(FACING));
            worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
        }
        return true;
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(POWERED)) {
            notifyNeighbors(worldIn, pos, state.getValue(FACING));
        }

        super.breakBlock(worldIn, pos, state);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        return !state.getValue(POWERED) ? 0 : (state.getValue(FACING) == side ? 15 : 0);
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower() {
        return true;
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (state.getValue(POWERED)) {
                if (wooden) {
                    checkForArrows(worldIn, pos, state);
                } else {
                    worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.FALSE));
                    notifyNeighbors(worldIn, pos, state.getValue(FACING));
                    worldIn.playSoundEffect((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, "random.click", 0.3F, 0.5F);
                    worldIn.markBlockRangeForRenderUpdate(pos, pos);
                }
            }
        }
    }

    /**
     * Sets the block's bounds for rendering it as an item
     */
    public void setBlockBoundsForItemRender() {
        float f = 0.1875F;
        float f1 = 0.125F;
        float f2 = 0.125F;
        setBlockBounds(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1, 0.5F + f2);
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote && wooden && !state.<Boolean>getValue(POWERED)) {
            checkForArrows(worldIn, pos, state);
        }
    }

    private void checkForArrows(World worldIn, BlockPos pos, IBlockState state) {
        updateBlockBounds(state);
        List<? extends Entity> list = worldIn.<Entity>getEntitiesWithinAABB(EntityArrow.class, new AxisAlignedBB((double) pos.getX() + minX, (double) pos.getY() + minY, (double) pos.getZ() + minZ, (double) pos.getX() + maxX, (double) pos.getY() + maxY, (double) pos.getZ() + maxZ));
        boolean flag = !list.isEmpty();
        boolean flag1 = state.<Boolean>getValue(POWERED);

        if (flag && !flag1) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.TRUE));
            notifyNeighbors(worldIn, pos, state.getValue(FACING));
            worldIn.markBlockRangeForRenderUpdate(pos, pos);
            worldIn.playSoundEffect((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
        }

        if (!flag && flag1) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.FALSE));
            notifyNeighbors(worldIn, pos, state.getValue(FACING));
            worldIn.markBlockRangeForRenderUpdate(pos, pos);
            worldIn.playSoundEffect((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, "random.click", 0.3F, 0.5F);
        }

        if (flag) {
            worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
        }
    }

    private void notifyNeighbors(World worldIn, BlockPos pos, EnumFacing facing) {
        worldIn.notifyNeighborsOfStateChange(pos, this);
        worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = switch (meta & 7) {
            case 0 -> EnumFacing.DOWN;
            case 1 -> EnumFacing.EAST;
            case 2 -> EnumFacing.WEST;
            case 3 -> EnumFacing.SOUTH;
            case 4 -> EnumFacing.NORTH;
            default -> EnumFacing.UP;
        };

        return getDefaultState().withProperty(FACING, enumfacing).withProperty(POWERED, (meta & 8) > 0);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        int i = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case WEST -> 2;
            case SOUTH -> 3;
            case NORTH -> 4;
            case DOWN -> 0;
            default -> 5;
        };

        if (state.<Boolean>getValue(POWERED)) {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, POWERED);
    }
}
