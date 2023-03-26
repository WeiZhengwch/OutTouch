package net.minecraft.world.biome;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;

import java.util.Random;

public class BiomeGenSavanna extends BiomeGenBase {
    private static final WorldGenSavannaTree field_150627_aC = new WorldGenSavannaTree(false);

    protected BiomeGenSavanna(int id) {
        super(id);
        spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityHorse.class, 1, 2, 6));
        theBiomeDecorator.treesPerChunk = 1;
        theBiomeDecorator.flowersPerChunk = 4;
        theBiomeDecorator.grassPerChunk = 20;
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return rand.nextInt(5) > 0 ? field_150627_aC : worldGeneratorTrees;
    }

    protected BiomeGenBase createMutatedBiome(int p_180277_1_) {
        BiomeGenBase biomegenbase = new BiomeGenSavanna.Mutated(p_180277_1_, this);
        biomegenbase.temperature = (temperature + 1.0F) * 0.5F;
        biomegenbase.minHeight = minHeight * 0.5F + 0.3F;
        biomegenbase.maxHeight = maxHeight * 0.5F + 1.2F;
        return biomegenbase;
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);

        for (int i = 0; i < 7; ++i) {
            int j = rand.nextInt(16) + 8;
            int k = rand.nextInt(16) + 8;
            int l = rand.nextInt(worldIn.getHeight(pos.add(j, 0, k)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j, l, k));
        }

        super.decorate(worldIn, rand, pos);
    }

    public static class Mutated extends BiomeGenMutated {
        public Mutated(int p_i45382_1_, BiomeGenBase p_i45382_2_) {
            super(p_i45382_1_, p_i45382_2_);
            theBiomeDecorator.treesPerChunk = 2;
            theBiomeDecorator.flowersPerChunk = 2;
            theBiomeDecorator.grassPerChunk = 5;
        }

        public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
            topBlock = Blocks.grass.getDefaultState();
            fillerBlock = Blocks.dirt.getDefaultState();

            if (noiseVal > 1.75D) {
                topBlock = Blocks.stone.getDefaultState();
                fillerBlock = Blocks.stone.getDefaultState();
            } else if (noiseVal > -0.5D) {
                topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
            }

            generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
        }

        public void decorate(World worldIn, Random rand, BlockPos pos) {
            theBiomeDecorator.decorate(worldIn, rand, this, pos);
        }
    }
}
