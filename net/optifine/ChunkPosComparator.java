package net.optifine;

import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.Comparator;

public class ChunkPosComparator implements Comparator<ChunkCoordIntPair> {
    private final int chunkPosX;
    private final int chunkPosZ;
    private final double yawRad;
    private final double pitchNorm;

    public ChunkPosComparator(int chunkPosX, int chunkPosZ, double yawRad, double pitchRad) {
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.yawRad = yawRad;
        pitchNorm = 1.0D - MathHelper.clamp_double(Math.abs(pitchRad) / (Math.PI / 2.0D), 0.0D, 1.0D);
    }

    public int compare(ChunkCoordIntPair cp1, ChunkCoordIntPair cp2) {
        int i = getDistSq(cp1);
        int j = getDistSq(cp2);
        return i - j;
    }

    private int getDistSq(ChunkCoordIntPair cp) {
        int i = cp.chunkXPos - chunkPosX;
        int j = cp.chunkZPos - chunkPosZ;
        int k = i * i + j * j;
        double d0 = MathHelper.atan2(j, i);
        double d1 = Math.abs(d0 - yawRad);

        if (d1 > Math.PI) {
            d1 = (Math.PI * 2.0D) - d1;
        }

        k = (int) ((double) k * 1000.0D * pitchNorm * d1 * d1);
        return k;
    }
}
