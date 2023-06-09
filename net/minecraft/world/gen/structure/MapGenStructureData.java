package net.minecraft.world.gen.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class MapGenStructureData extends WorldSavedData {
    private NBTTagCompound tagCompound = new NBTTagCompound();

    public MapGenStructureData(String name) {
        super(name);
    }

    public static String formatChunkCoords(int chunkX, int chunkZ) {
        return "[" + chunkX + "," + chunkZ + "]";
    }

    /**
     * reads in data from the NBTTagCompound into this MapDataBase
     */
    public void readFromNBT(NBTTagCompound nbt) {
        tagCompound = nbt.getCompoundTag("Features");
    }

    /**
     * write data to NBTTagCompound from this MapDataBase, similar to Entities and TileEntities
     */
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Features", tagCompound);
    }

    /**
     * Writes the NBT tag of an instance of this structure type to the internal NBT tag, using the chunkcoordinates as
     * the key
     */
    public void writeInstance(NBTTagCompound tagCompoundIn, int chunkX, int chunkZ) {
        tagCompound.setTag(formatChunkCoords(chunkX, chunkZ), tagCompoundIn);
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }
}
