package net.minecraft.world.gen.feature;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenLiquids extends WorldGenerator {
    private final Block block;

    public WorldGenLiquids(Block p_i45465_1_) {
        block = p_i45465_1_;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (worldIn.getBlockState(position.up()).getBlock() != Blocks.stone) {
            return false;
        } else if (worldIn.getBlockState(position.down()).getBlock() != Blocks.stone) {
            return false;
        } else if (worldIn.getBlockState(position).getBlock().getMaterial() != Material.air && worldIn.getBlockState(position).getBlock() != Blocks.stone) {
            return false;
        } else {
            int i = 0;

            if (worldIn.getBlockState(position.west()).getBlock() == Blocks.stone) {
                ++i;
            }

            if (worldIn.getBlockState(position.east()).getBlock() == Blocks.stone) {
                ++i;
            }

            if (worldIn.getBlockState(position.north()).getBlock() == Blocks.stone) {
                ++i;
            }

            if (worldIn.getBlockState(position.south()).getBlock() == Blocks.stone) {
                ++i;
            }

            int j = 0;

            if (worldIn.isAirBlock(position.west())) {
                ++j;
            }

            if (worldIn.isAirBlock(position.east())) {
                ++j;
            }

            if (worldIn.isAirBlock(position.north())) {
                ++j;
            }

            if (worldIn.isAirBlock(position.south())) {
                ++j;
            }

            if (i == 3 && j == 1) {
                worldIn.setBlockState(position, block.getDefaultState(), 2);
                worldIn.forceBlockUpdateTick(block, position, rand);
            }

            return true;
        }
    }
}
