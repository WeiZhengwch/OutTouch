package net.minecraft.world.biome;

import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;

public class BiomeGenHell extends BiomeGenBase {
    public BiomeGenHell(int id) {
        super(id);
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
        spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityGhast.class, 50, 4, 4));
        spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityPigZombie.class, 100, 4, 4));
        spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityMagmaCube.class, 1, 4, 4));
    }
}
