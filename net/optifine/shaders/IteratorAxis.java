package net.optifine.shaders;

import net.minecraft.util.BlockPos;
import net.optifine.BlockPosM;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorAxis implements Iterator<BlockPos> {
    private final double yDelta;
    private final double zDelta;
    private final int xStart;
    private final int xEnd;
    private final BlockPosM pos = new BlockPosM(0, 0, 0);
    private double yStart;
    private double yEnd;
    private double zStart;
    private double zEnd;
    private int xNext;
    private double yNext;
    private double zNext;
    private boolean hasNext;

    public IteratorAxis(BlockPos posStart, BlockPos posEnd, double yDelta, double zDelta) {
        this.yDelta = yDelta;
        this.zDelta = zDelta;
        xStart = posStart.getX();
        xEnd = posEnd.getX();
        yStart = posStart.getY();
        yEnd = (double) posEnd.getY() - 0.5D;
        zStart = posStart.getZ();
        zEnd = (double) posEnd.getZ() - 0.5D;
        xNext = xStart;
        yNext = yStart;
        zNext = zStart;
        hasNext = xNext < xEnd && yNext < yEnd && zNext < zEnd;
    }

    public static void main(String[] args) {
        BlockPos blockpos = new BlockPos(-2, 10, 20);
        BlockPos blockpos1 = new BlockPos(2, 12, 22);
        double d0 = -0.5D;
        double d1 = 0.5D;
        IteratorAxis iteratoraxis = new IteratorAxis(blockpos, blockpos1, d0, d1);
        System.out.println("Start: " + blockpos + ", end: " + blockpos1 + ", yDelta: " + d0 + ", zDelta: " + d1);

        while (iteratoraxis.hasNext()) {
            BlockPos blockpos2 = iteratoraxis.next();
            System.out.println(blockpos2);
        }
    }

    public boolean hasNext() {
        return hasNext;
    }

    public BlockPos next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        } else {
            pos.setXyz(xNext, yNext, zNext);
            nextPos();
            hasNext = xNext < xEnd && yNext < yEnd && zNext < zEnd;
            return pos;
        }
    }

    private void nextPos() {
        ++zNext;

        if (zNext >= zEnd) {
            zNext = zStart;
            ++yNext;

            if (yNext >= yEnd) {
                yNext = yStart;
                yStart += yDelta;
                yEnd += yDelta;
                yNext = yStart;
                zStart += zDelta;
                zEnd += zDelta;
                zNext = zStart;
                ++xNext;

                if (xNext >= xEnd) {
                }
            }
        }
    }

    public void remove() {
        throw new RuntimeException("Not implemented");
    }
}
