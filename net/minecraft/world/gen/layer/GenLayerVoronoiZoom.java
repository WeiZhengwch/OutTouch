package net.minecraft.world.gen.layer;

public class GenLayerVoronoiZoom extends GenLayer {
    public GenLayerVoronoiZoom(long p_i2133_1_, GenLayer p_i2133_3_) {
        super(p_i2133_1_);
        super.parent = p_i2133_3_;
    }

    /**
     * Returns a list of integer values generated by this layer. These may be interpreted as temperatures, rainfall
     * amounts, or biomeList[] indices based on the particular GenLayer subclass.
     */
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        areaX = areaX - 2;
        areaY = areaY - 2;
        int i = areaX >> 2;
        int j = areaY >> 2;
        int k = (areaWidth >> 2) + 2;
        int l = (areaHeight >> 2) + 2;
        int[] aint = parent.getInts(i, j, k, l);
        int i1 = k - 1 << 2;
        int j1 = l - 1 << 2;
        int[] aint1 = IntCache.getIntCache(i1 * j1);

        for (int k1 = 0; k1 < l - 1; ++k1) {
            int l1 = 0;
            int i2 = aint[l1 + (k1) * k];

            for (int j2 = aint[l1 + (k1 + 1) * k]; l1 < k - 1; ++l1) {
                double d0 = 3.6D;
                initChunkSeed((long) l1 + i << 2, (long) k1 + j << 2);
                double d1 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                double d2 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                initChunkSeed((long) l1 + i + 1 << 2, (long) k1 + j << 2);
                double d3 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                double d4 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                initChunkSeed((long) l1 + i << 2, (long) k1 + j + 1 << 2);
                double d5 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                double d6 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                initChunkSeed((long) l1 + i + 1 << 2, (long) k1 + j + 1 << 2);
                double d7 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                double d8 = ((double) nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                int k2 = aint[l1 + 1 + (k1) * k] & 255;
                int l2 = aint[l1 + 1 + (k1 + 1) * k] & 255;

                for (int i3 = 0; i3 < 4; ++i3) {
                    int j3 = ((k1 << 2) + i3) * i1 + (l1 << 2);

                    for (int k3 = 0; k3 < 4; ++k3) {
                        double d9 = ((double) i3 - d2) * ((double) i3 - d2) + ((double) k3 - d1) * ((double) k3 - d1);
                        double d10 = ((double) i3 - d4) * ((double) i3 - d4) + ((double) k3 - d3) * ((double) k3 - d3);
                        double d11 = ((double) i3 - d6) * ((double) i3 - d6) + ((double) k3 - d5) * ((double) k3 - d5);
                        double d12 = ((double) i3 - d8) * ((double) i3 - d8) + ((double) k3 - d7) * ((double) k3 - d7);

                        if (d9 < d10 && d9 < d11 && d9 < d12) {
                            aint1[j3++] = i2;
                        } else if (d10 < d9 && d10 < d11 && d10 < d12) {
                            aint1[j3++] = k2;
                        } else if (d11 < d9 && d11 < d10 && d11 < d12) {
                            aint1[j3++] = j2;
                        } else {
                            aint1[j3++] = l2;
                        }
                    }
                }

                i2 = k2;
                j2 = l2;
            }
        }

        int[] aint2 = IntCache.getIntCache(areaWidth * areaHeight);

        for (int l3 = 0; l3 < areaHeight; ++l3) {
            System.arraycopy(aint1, (l3 + (areaY & 3)) * i1 + (areaX & 3), aint2, l3 * areaWidth, areaWidth);
        }

        return aint2;
    }
}
