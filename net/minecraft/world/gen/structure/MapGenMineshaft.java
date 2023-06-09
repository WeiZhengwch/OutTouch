package net.minecraft.world.gen.structure;

import net.minecraft.util.MathHelper;

import java.util.Map;
import java.util.Map.Entry;

public class MapGenMineshaft extends MapGenStructure {
    private double field_82673_e = 0.004D;

    public MapGenMineshaft() {
    }

    public MapGenMineshaft(Map<String, String> p_i2034_1_) {
        for (Entry<String, String> entry : p_i2034_1_.entrySet()) {
            if (entry.getKey().equals("chance")) {
                field_82673_e = MathHelper.parseDoubleWithDefault(entry.getValue(), field_82673_e);
            }
        }
    }

    public String getStructureName() {
        return "Mineshaft";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        return rand.nextDouble() < field_82673_e && rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new StructureMineshaftStart(worldObj, rand, chunkX, chunkZ);
    }
}
