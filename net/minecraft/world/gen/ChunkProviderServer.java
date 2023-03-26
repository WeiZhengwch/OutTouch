package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkProviderServer implements IChunkProvider {
    private static final Logger logger = LogManager.getLogger();
    private final Set<Long> droppedChunksSet = Collections.<Long>newSetFromMap(new ConcurrentHashMap());

    /**
     * a dummy chunk, returned in place of an actual chunk.
     */
    private final Chunk dummyChunk;

    /**
     * chunk generator object. Calls to load nonexistent chunks are forwarded to this object.
     */
    private final IChunkProvider serverChunkGenerator;
    private final IChunkLoader chunkLoader;
    private final LongHashMap<Chunk> id2ChunkMap = new LongHashMap();
    private final List<Chunk> loadedChunks = Lists.newArrayList();
    private final WorldServer worldObj;
    /**
     * if set, this flag forces a request to load a chunk to load the chunk rather than defaulting to the dummy if
     * possible
     */
    public boolean chunkLoadOverride = true;

    public ChunkProviderServer(WorldServer p_i1520_1_, IChunkLoader p_i1520_2_, IChunkProvider p_i1520_3_) {
        dummyChunk = new EmptyChunk(p_i1520_1_, 0, 0);
        worldObj = p_i1520_1_;
        chunkLoader = p_i1520_2_;
        serverChunkGenerator = p_i1520_3_;
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean chunkExists(int x, int z) {
        return id2ChunkMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

    public List<Chunk> func_152380_a() {
        return loadedChunks;
    }

    public void dropChunk(int x, int z) {
        if (worldObj.provider.canRespawnHere()) {
            if (!worldObj.isSpawnChunk(x, z)) {
                droppedChunksSet.add(ChunkCoordIntPair.chunkXZ2Int(x, z));
            }
        } else {
            droppedChunksSet.add(ChunkCoordIntPair.chunkXZ2Int(x, z));
        }
    }

    /**
     * marks all chunks for unload, ignoring those near the spawn
     */
    public void unloadAllChunks() {
        for (Chunk chunk : loadedChunks) {
            dropChunk(chunk.xPosition, chunk.zPosition);
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     *
     * @param chunkX x coord of the chunk to load (block coord >> 4)
     * @param chunkZ z coord of the chunk to load (block coord >> 4)
     */
    public Chunk loadChunk(int chunkX, int chunkZ) {
        long i = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
        droppedChunksSet.remove(i);
        Chunk chunk = id2ChunkMap.getValueByKey(i);

        if (chunk == null) {
            chunk = loadChunkFromFile(chunkX, chunkZ);

            if (chunk == null) {
                if (serverChunkGenerator == null) {
                    chunk = dummyChunk;
                } else {
                    try {
                        chunk = serverChunkGenerator.provideChunk(chunkX, chunkZ);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                        crashreportcategory.addCrashSection("Location", String.format("%d,%d", chunkX, chunkZ));
                        crashreportcategory.addCrashSection("Position hash", i);
                        crashreportcategory.addCrashSection("Generator", serverChunkGenerator.makeString());
                        throw new ReportedException(crashreport);
                    }
                }
            }

            id2ChunkMap.add(i, chunk);
            loadedChunks.add(chunk);
            chunk.onChunkLoad();
            chunk.populateChunk(this, this, chunkX, chunkZ);
        }

        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? (!worldObj.isFindingSpawnPoint() && !chunkLoadOverride ? dummyChunk : loadChunk(x, z)) : chunk;
    }

    private Chunk loadChunkFromFile(int x, int z) {
        if (chunkLoader == null) {
            return null;
        } else {
            try {
                Chunk chunk = chunkLoader.loadChunk(worldObj, x, z);

                if (chunk != null) {
                    chunk.setLastSaveTime(worldObj.getTotalWorldTime());

                    if (serverChunkGenerator != null) {
                        serverChunkGenerator.recreateStructures(chunk, x, z);
                    }
                }

                return chunk;
            } catch (Exception exception) {
                logger.error("Couldn't load chunk", exception);
                return null;
            }
        }
    }

    private void saveChunkExtraData(Chunk chunkIn) {
        if (chunkLoader != null) {
            try {
                chunkLoader.saveExtraChunkData(worldObj, chunkIn);
            } catch (Exception exception) {
                logger.error("Couldn't save entities", exception);
            }
        }
    }

    private void saveChunkData(Chunk chunkIn) {
        if (chunkLoader != null) {
            try {
                chunkIn.setLastSaveTime(worldObj.getTotalWorldTime());
                chunkLoader.saveChunk(worldObj, chunkIn);
            } catch (MinecraftException minecraftexception) {
                logger.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
            }
        }
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        Chunk chunk = provideChunk(x, z);

        if (!chunk.isTerrainPopulated()) {
            chunk.func_150809_p();

            if (serverChunkGenerator != null) {
                serverChunkGenerator.populate(chunkProvider, x, z);
                chunk.setChunkModified();
            }
        }
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z) {
        if (serverChunkGenerator != null && serverChunkGenerator.populateChunk(chunkProvider, chunkIn, x, z)) {
            Chunk chunk = provideChunk(x, z);
            chunk.setChunkModified();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback) {
        int i = 0;
        List<Chunk> list = Lists.newArrayList(loadedChunks);

        for (Chunk chunk : list) {
            if (saveAllChunks) {
                saveChunkExtraData(chunk);
            }

            if (chunk.needsSaving(saveAllChunks)) {
                saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !saveAllChunks) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData() {
        if (chunkLoader != null) {
            chunkLoader.saveExtraData();
        }
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks() {
        if (!worldObj.disableLevelSaving) {
            for (int i = 0; i < 100; ++i) {
                if (!droppedChunksSet.isEmpty()) {
                    Long olong = droppedChunksSet.iterator().next();
                    Chunk chunk = id2ChunkMap.getValueByKey(olong);

                    if (chunk != null) {
                        chunk.onChunkUnload();
                        saveChunkData(chunk);
                        saveChunkExtraData(chunk);
                        id2ChunkMap.remove(olong);
                        loadedChunks.remove(chunk);
                    }

                    droppedChunksSet.remove(olong);
                }
            }

            if (chunkLoader != null) {
                chunkLoader.chunkTick();
            }
        }

        return serverChunkGenerator.unloadQueuedChunks();
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave() {
        return !worldObj.disableLevelSaving;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString() {
        return "ServerChunkCache: " + id2ChunkMap.getNumHashElements() + " Drop: " + droppedChunksSet.size();
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return serverChunkGenerator.getPossibleCreatures(creatureType, pos);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return serverChunkGenerator.getStrongholdGen(worldIn, structureName, position);
    }

    public int getLoadedChunkCount() {
        return id2ChunkMap.getNumHashElements();
    }

    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }

    public Chunk provideChunk(BlockPos blockPosIn) {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
