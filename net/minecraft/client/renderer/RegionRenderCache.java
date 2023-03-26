package net.minecraft.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.optifine.DynamicLights;

import java.util.ArrayDeque;
import java.util.Arrays;

public class RegionRenderCache extends ChunkCache {
    private static final ArrayDeque<int[]> cacheLights = new ArrayDeque<>();
    private static final ArrayDeque<IBlockState[]> cacheStates = new ArrayDeque<>();
    private static final int maxCacheSize = Config.limit(Runtime.getRuntime().availableProcessors(), 1, 32);
    private final BlockPos position;
    private final int[] combinedLights;
    private final IBlockState[] blockStates;

    public RegionRenderCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn) {
        super(worldIn, posFromIn, posToIn, subIn);
        position = posFromIn.subtract(new Vec3i(subIn, subIn, subIn));
        combinedLights = allocateLights();
        Arrays.fill(combinedLights, -1);
        blockStates = allocateStates();
    }

    private static int[] allocateLights() {
        synchronized (cacheLights) {
            int[] aint = cacheLights.pollLast();

            if (aint == null || aint.length < 8000) {
                aint = new int[8000];
            }

            return aint;
        }
    }

    public static void freeLights(int[] p_freeLights_0_) {
        synchronized (cacheLights) {
            if (cacheLights.size() < maxCacheSize) {
                cacheLights.add(p_freeLights_0_);
            }
        }
    }

    private static IBlockState[] allocateStates() {
        synchronized (cacheStates) {
            IBlockState[] aiblockstate = cacheStates.pollLast();

            if (aiblockstate != null && aiblockstate.length >= 8000) {
                Arrays.fill(aiblockstate, null);
            } else {
                aiblockstate = new IBlockState[8000];
            }

            return aiblockstate;
        }
    }

    public static void freeStates(IBlockState[] p_freeStates_0_) {
        synchronized (cacheStates) {
            if (cacheStates.size() < maxCacheSize) {
                cacheStates.add(p_freeStates_0_);
            }
        }
    }

    public TileEntity getTileEntity(BlockPos pos) {
        int i = (pos.getX() >> 4) - chunkX;
        int j = (pos.getZ() >> 4) - chunkZ;
        return chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.QUEUED);
    }

    public int getCombinedLight(BlockPos pos, int lightValue) {
        int i = getPositionIndex(pos);
        int j = combinedLights[i];

        if (j == -1) {
            j = super.getCombinedLight(pos, lightValue);

            if (Config.isDynamicLights() && !getBlockState(pos).getBlock().isOpaqueCube()) {
                j = DynamicLights.getCombinedLight(pos, j);
            }

            combinedLights[i] = j;
        }

        return j;
    }

    public IBlockState getBlockState(BlockPos pos) {
        int i = getPositionIndex(pos);
        IBlockState iblockstate = blockStates[i];

        if (iblockstate == null) {
            iblockstate = getBlockStateRaw(pos);
            blockStates[i] = iblockstate;
        }

        return iblockstate;
    }

    private IBlockState getBlockStateRaw(BlockPos pos) {
        return super.getBlockState(pos);
    }

    private int getPositionIndex(BlockPos p_175630_1_) {
        int i = p_175630_1_.getX() - position.getX();
        int j = p_175630_1_.getY() - position.getY();
        int k = p_175630_1_.getZ() - position.getZ();
        return i * 400 + k * 20 + j;
    }

    public void freeBuffers() {
        freeLights(combinedLights);
        freeStates(blockStates);
    }
}
