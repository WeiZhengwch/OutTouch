package net.optifine.util;

public class CacheLocal {
    private int maxX = 18;
    private int maxY = 128;
    private int maxZ = 18;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private final int[][][] cache;
    private int[] lastZs;
    private int lastDz;

    public CacheLocal(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        cache = new int[maxX][maxY][maxZ];
        resetCache();
    }

    public void resetCache() {
        for (int i = 0; i < maxX; ++i) {
            int[][] aint = cache[i];

            for (int j = 0; j < maxY; ++j) {
                int[] aint1 = aint[j];

                for (int k = 0; k < maxZ; ++k) {
                    aint1[k] = -1;
                }
            }
        }
    }

    public void setOffset(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        resetCache();
    }

    public int get(int x, int y, int z) {
        try {
            lastZs = cache[x - offsetX][y - offsetY];
            lastDz = z - offsetZ;
            return lastZs[lastDz];
        } catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
            arrayindexoutofboundsexception.printStackTrace();
            return -1;
        }
    }

    public void setLast(int val) {
        try {
            lastZs[lastDz] = val;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
