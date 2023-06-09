package net.optifine;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public class BlockPosM extends BlockPos {
    private final int level;
    private int mx;
    private int my;
    private int mz;
    private BlockPosM[] facings;
    private boolean needsUpdate;

    public BlockPosM(int x, int y, int z) {
        this(x, y, z, 0);
    }

    public BlockPosM(double xIn, double yIn, double zIn) {
        this(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public BlockPosM(int x, int y, int z, int level) {
        super(0, 0, 0);
        mx = x;
        my = y;
        mz = z;
        this.level = level;
    }

    public static Iterable getAllInBoxMutable(BlockPos from, BlockPos to) {
        final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return () -> new AbstractIterator() {
            private BlockPosM theBlockPosM;

            private BlockPosM computeNext0() {
                if (theBlockPosM == null) {
                    theBlockPosM = new BlockPosM(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 3);
                    return theBlockPosM;
                } else if (theBlockPosM.equals(blockpos1)) {
                    return (BlockPosM) endOfData();
                } else {
                    int i = theBlockPosM.getX();
                    int j = theBlockPosM.getY();
                    int k = theBlockPosM.getZ();

                    if (i < blockpos1.getX()) {
                        ++i;
                    } else if (j < blockpos1.getY()) {
                        i = blockpos.getX();
                        ++j;
                    } else if (k < blockpos1.getZ()) {
                        i = blockpos.getX();
                        j = blockpos.getY();
                        ++k;
                    }

                    theBlockPosM.setXyz(i, j, k);
                    return theBlockPosM;
                }
            }

            protected Object computeNext() {
                return computeNext0();
            }
        };
    }

    /**
     * Get the X coordinate
     */
    public int getX() {
        return mx;
    }

    /**
     * Get the Y coordinate
     */
    public int getY() {
        return my;
    }

    /**
     * Get the Z coordinate
     */
    public int getZ() {
        return mz;
    }

    public void setXyz(int x, int y, int z) {
        mx = x;
        my = y;
        mz = z;
        needsUpdate = true;
    }

    public void setXyz(double xIn, double yIn, double zIn) {
        setXyz(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public BlockPosM set(Vec3i vec) {
        setXyz(vec.getX(), vec.getY(), vec.getZ());
        return this;
    }

    public BlockPosM set(int xIn, int yIn, int zIn) {
        setXyz(xIn, yIn, zIn);
        return this;
    }

    public BlockPos offsetMutable(EnumFacing facing) {
        return offset(facing);
    }

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos offset(EnumFacing facing) {
        if (level <= 0) {
            return super.offset(facing, 1);
        } else {
            if (facings == null) {
                facings = new BlockPosM[EnumFacing.VALUES.length];
            }

            if (needsUpdate) {
                update();
            }

            int i = facing.getIndex();
            BlockPosM blockposm = facings[i];

            if (blockposm == null) {
                int j = mx + facing.getFrontOffsetX();
                int k = my + facing.getFrontOffsetY();
                int l = mz + facing.getFrontOffsetZ();
                blockposm = new BlockPosM(j, k, l, level - 1);
                facings[i] = blockposm;
            }

            return blockposm;
        }
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     */
    public BlockPos offset(EnumFacing facing, int n) {
        return n == 1 ? offset(facing) : super.offset(facing, n);
    }

    private void update() {
        for (int i = 0; i < 6; ++i) {
            BlockPosM blockposm = facings[i];

            if (blockposm != null) {
                EnumFacing enumfacing = EnumFacing.VALUES[i];
                int j = mx + enumfacing.getFrontOffsetX();
                int k = my + enumfacing.getFrontOffsetY();
                int l = mz + enumfacing.getFrontOffsetZ();
                blockposm.setXyz(j, k, l);
            }
        }

        needsUpdate = false;
    }

    public BlockPos toImmutable() {
        return new BlockPos(mx, my, mz);
    }
}
