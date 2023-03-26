package net.optifine.util;

public class CacheLocalByte {
    private int maxX = 18;
    private int maxY = 128;
    private int maxZ = 18;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private final byte[][][] cache;
    private byte[] lastZs;
    private int lastDz;

    public CacheLocalByte(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        cache = new byte[maxX][maxY][maxZ];
        resetCache();
    }

    public void resetCache() {
        for (int i = 0; i < maxX; ++i) {
            byte[][] abyte = cache[i];

            for (int j = 0; j < maxY; ++j) {
                byte[] abyte1 = abyte[j];

                for (int k = 0; k < maxZ; ++k) {
                    abyte1[k] = -1;
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

    public byte get(int x, int y, int z) {
        try {
            lastZs = cache[x - offsetX][y - offsetY];
            lastDz = z - offsetZ;
            return lastZs[lastDz];
        } catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
            arrayindexoutofboundsexception.printStackTrace();
            return (byte) -1;
        }
    }

    public void setLast(byte val) {
        try {
            lastZs[lastDz] = val;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
