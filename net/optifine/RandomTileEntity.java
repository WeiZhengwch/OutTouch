package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.util.TileEntityUtils;

public class RandomTileEntity implements IRandomEntity {
    private TileEntity tileEntity;

    public int getId() {
        return Config.getRandom(tileEntity.getPos(), tileEntity.getBlockMetadata());
    }

    public BlockPos getSpawnPosition() {
        return tileEntity.getPos();
    }

    public String getName() {
        String s = TileEntityUtils.getTileEntityName(tileEntity);
        return s;
    }

    public BiomeGenBase getSpawnBiome() {
        return tileEntity.getWorld().getBiomeGenForCoords(tileEntity.getPos());
    }

    public int getHealth() {
        return -1;
    }

    public int getMaxHealth() {
        return -1;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public void setTileEntity(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }
}
