package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.ClearWater;
import net.optifine.reflect.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.FutureTask;

public class IntegratedServer extends MinecraftServer {
    private static final Logger logger = LogManager.getLogger();

    /**
     * The Minecraft instance.
     */
    private final Minecraft mc;
    private final WorldSettings theWorldSettings;
    public World difficultyUpdateWorld;
    public BlockPos difficultyUpdatePos;
    public DifficultyInstance difficultyLast;
    private boolean isGamePaused;
    private boolean isPublic;
    private ThreadLanServerPing lanServerPing;
    private long ticksSaveLast;

    public IntegratedServer(Minecraft mcIn) {
        super(mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
        mc = mcIn;
        theWorldSettings = null;
    }

    public IntegratedServer(Minecraft mcIn, String folderName, String worldName, WorldSettings settings) {
        super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
        setServerOwner(mcIn.getSession().getUsername());
        setFolderName(folderName);
        setWorldName(worldName);
        canCreateBonusChest(settings.isBonusChestEnabled());
        setBuildLimit(256);
        setConfigManager(new IntegratedPlayerList(this));
        mc = mcIn;
        theWorldSettings = isDemo() ? DemoWorldServer.demoWorldSettings : settings;
        ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo != null) {
            NBTTagCompound nbttagcompound = worldinfo.getPlayerNBTTagCompound();

            if (nbttagcompound != null && nbttagcompound.hasKey("Dimension")) {
                int i = nbttagcompound.getInteger("Dimension");
                PacketThreadUtil.lastDimensionId = i;
                mc.loadingScreen.setLoadingProgress(-1);
            }
        }
    }

    protected ServerCommandManager createNewCommandManager() {
        return new IntegratedServerCommandManager();
    }

    protected void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String worldNameIn2) {
        convertMapIfNeeded(saveName);
        boolean flag = Reflector.DimensionManager.exists();

        if (!flag) {
            worldServers = new WorldServer[3];
            timeOfLastDimensionTick = new long[worldServers.length][100];
        }

        ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(saveName, true);
        setResourcePackFromWorld(getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null) {
            worldinfo = new WorldInfo(theWorldSettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
        }

        if (flag) {
            WorldServer worldserver = isDemo() ? (WorldServer) (new DemoWorldServer(this, isavehandler, worldinfo, 0, theProfiler)).init() : (WorldServer) (new WorldServer(this, isavehandler, worldinfo, 0, theProfiler)).init();
            worldserver.initialize(theWorldSettings);
            Integer[] ainteger = (Integer[]) Reflector.call(Reflector.DimensionManager_getStaticDimensionIDs, new Object[0]);
            Integer[] ainteger1 = ainteger;
            int i = ainteger.length;

            for (int j = 0; j < i; ++j) {
                int k = ainteger1[j];
                WorldServer worldserver1 = k == 0 ? worldserver : (WorldServer) (new WorldServerMulti(this, isavehandler, k, worldserver, theProfiler)).init();
                worldserver1.addWorldAccess(new WorldManager(this, worldserver1));

                if (!isSinglePlayer()) {
                    worldserver1.getWorldInfo().setGameType(getGameType());
                }

                if (Reflector.EventBus.exists()) {
                    Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, worldserver1);
                }
            }

            getConfigurationManager().setPlayerManager(new WorldServer[]{worldserver});

            if (worldserver.getWorldInfo().getDifficulty() == null) {
                setDifficultyForAllWorlds(mc.gameSettings.difficulty);
            }
        } else {
            for (int l = 0; l < worldServers.length; ++l) {
                int i1 = 0;

                if (l == 1) {
                    i1 = -1;
                }

                if (l == 2) {
                    i1 = 1;
                }

                if (l == 0) {
                    if (isDemo()) {
                        worldServers[0] = (WorldServer) (new DemoWorldServer(this, isavehandler, worldinfo, i1, theProfiler)).init();
                    } else {
                        worldServers[0] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, i1, theProfiler)).init();
                    }

                    worldServers[0].initialize(theWorldSettings);
                } else {
                    worldServers[l] = (WorldServer) (new WorldServerMulti(this, isavehandler, i1, worldServers[0], theProfiler)).init();
                }

                worldServers[l].addWorldAccess(new WorldManager(this, worldServers[l]));
            }

            getConfigurationManager().setPlayerManager(worldServers);

            if (worldServers[0].getWorldInfo().getDifficulty() == null) {
                setDifficultyForAllWorlds(mc.gameSettings.difficulty);
            }
        }

        initialWorldChunkLoad();
    }

    /**
     * Initialises the server and starts it.
     */
    protected boolean startServer() {
        logger.info("Starting integrated minecraft server version 1.9");
        setOnlineMode(true);
        setCanSpawnAnimals(true);
        setCanSpawnNPCs(true);
        setAllowPvp(true);
        setAllowFlight(true);
        logger.info("Generating keypair");
        setKeyPair(CryptManager.generateKeyPair());

        if (Reflector.FMLCommonHandler_handleServerAboutToStart.exists()) {
            Object object = Reflector.call(Reflector.FMLCommonHandler_instance);

            if (!Reflector.callBoolean(object, Reflector.FMLCommonHandler_handleServerAboutToStart, this)) {
                return false;
            }
        }

        loadAllWorlds(getFolderName(), getWorldName(), theWorldSettings.getSeed(), theWorldSettings.getTerrainType(), theWorldSettings.getWorldName());
        setMOTD(getServerOwner() + " - " + worldServers[0].getWorldInfo().getWorldName());

        if (Reflector.FMLCommonHandler_handleServerStarting.exists()) {
            Object object1 = Reflector.call(Reflector.FMLCommonHandler_instance);

            if (Reflector.FMLCommonHandler_handleServerStarting.getReturnType() == Boolean.TYPE) {
                return Reflector.callBoolean(object1, Reflector.FMLCommonHandler_handleServerStarting, this);
            }

            Reflector.callVoid(object1, Reflector.FMLCommonHandler_handleServerStarting, this);
        }

        return true;
    }

    /**
     * Main function called by run() every loop.
     */
    public void tick() {
        onTick();
        boolean flag = isGamePaused;
        isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();

        if (!flag && isGamePaused) {
            logger.info("Saving and pausing game...");
            getConfigurationManager().saveAllPlayerData();
            saveAllWorlds(false);
        }

        if (isGamePaused) {
            synchronized (futureTaskQueue) {
                while (!futureTaskQueue.isEmpty()) {
                    Util.runTask((FutureTask) futureTaskQueue.poll(), logger);
                }
            }
        } else {
            super.tick();

            if (mc.gameSettings.renderDistanceChunks != getConfigurationManager().getViewDistance()) {
                logger.info("Changing view distance to {}, from {}", new Object[]{mc.gameSettings.renderDistanceChunks, getConfigurationManager().getViewDistance()});
                getConfigurationManager().setViewDistance(mc.gameSettings.renderDistanceChunks);
            }

            if (mc.theWorld != null) {
                WorldInfo worldinfo1 = worldServers[0].getWorldInfo();
                WorldInfo worldinfo = mc.theWorld.getWorldInfo();

                if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
                    logger.info("Changing difficulty to {}, from {}", new Object[]{worldinfo.getDifficulty(), worldinfo1.getDifficulty()});
                    setDifficultyForAllWorlds(worldinfo.getDifficulty());
                } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
                    logger.info("Locking difficulty to {}", new Object[]{worldinfo.getDifficulty()});

                    for (WorldServer worldserver : worldServers) {
                        if (worldserver != null) {
                            worldserver.getWorldInfo().setDifficultyLocked(true);
                        }
                    }
                }
            }
        }
    }

    public boolean canStructuresSpawn() {
        return false;
    }

    public WorldSettings.GameType getGameType() {
        return theWorldSettings.getGameType();
    }

    /**
     * Sets the game type for all worlds.
     */
    public void setGameType(WorldSettings.GameType gameMode) {
        getConfigurationManager().setGameType(gameMode);
    }

    /**
     * Get the server's difficulty
     */
    public EnumDifficulty getDifficulty() {
        return mc.theWorld == null ? mc.gameSettings.difficulty : mc.theWorld.getWorldInfo().getDifficulty();
    }

    /**
     * Defaults to false.
     */
    public boolean isHardcore() {
        return theWorldSettings.getHardcoreEnabled();
    }

    /**
     * Get if RCON command events should be broadcast to ops
     */
    public boolean shouldBroadcastRconToOps() {
        return true;
    }

    /**
     * Get if console command events should be broadcast to ops
     */
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    /**
     * par1 indicates if a log message should be output.
     */
    public void saveAllWorlds(boolean dontLog) {
        if (dontLog) {
            int i = getTickCounter();
            int j = mc.gameSettings.ofAutoSaveTicks;

            if ((long) i < ticksSaveLast + (long) j) {
                return;
            }

            ticksSaveLast = i;
        }

        super.saveAllWorlds(dontLog);
    }

    public File getDataDirectory() {
        return mc.mcDataDir;
    }

    public boolean isDedicatedServer() {
        return false;
    }

    /**
     * Get if native transport should be used. Native transport means linux server performance improvements and
     * optimized packet sending/receiving on linux
     */
    public boolean shouldUseNativeTransport() {
        return false;
    }

    /**
     * Called on exit from the main run() loop.
     */
    protected void finalTick(CrashReport report) {
        mc.crashed(report);
    }

    /**
     * Adds the server info, including from theWorldServer, to the crash report.
     */
    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addCrashSectionCallable("Type", () -> "Integrated Server (map_client.txt)");
        report.getCategory().addCrashSectionCallable("Is Modded", () -> {
            String s = ClientBrandRetriever.getClientModName();

            if (!s.equals("vanilla")) {
                return "Definitely; Client brand changed to '" + s + "'";
            } else {
                s = getServerModName();
                return !s.equals("vanilla") ? "Definitely; Server brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
            }
        });
        return report;
    }

    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        super.setDifficultyForAllWorlds(difficulty);

        if (mc.theWorld != null) {
            mc.theWorld.getWorldInfo().setDifficulty(difficulty);
        }
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
        super.addServerStatsToSnooper(playerSnooper);
        playerSnooper.addClientStat("snooper_partner", mc.getPlayerUsageSnooper().getUniqueID());
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled() {
        return Minecraft.getMinecraft().isSnooperEnabled();
    }

    /**
     * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
     */
    public String shareToLAN(WorldSettings.GameType type, boolean allowCheats) {
        try {
            int i = -1;

            try {
                i = HttpUtil.getSuitableLanPort();
            } catch (IOException var5) {
            }

            if (i <= 0) {
                i = 25564;
            }

            getNetworkSystem().addLanEndpoint(null, i);
            logger.info("Started on " + i);
            isPublic = true;
            lanServerPing = new ThreadLanServerPing(getMOTD(), String.valueOf(i));
            lanServerPing.start();
            getConfigurationManager().setGameType(type);
            getConfigurationManager().setCommandsAllowedForAll(allowCheats);
            return String.valueOf(i);
        } catch (IOException var6) {
            return null;
        }
    }

    /**
     * Saves all necessary data as preparation for stopping the server.
     */
    public void stopServer() {
        super.stopServer();

        if (lanServerPing != null) {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    /**
     * Sets the serverRunning variable to false, in order to get the server to shut down.
     */
    public void initiateShutdown() {
        if (!Reflector.MinecraftForge.exists() || isServerRunning()) {
            Futures.getUnchecked(addScheduledTask(() -> {
                for (EntityPlayerMP entityplayermp : Lists.newArrayList(getConfigurationManager().getPlayerList())) {
                    getConfigurationManager().playerLoggedOut(entityplayermp);
                }
            }));
        }

        super.initiateShutdown();

        if (lanServerPing != null) {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    public void setStaticInstance() {
        setInstance();
    }

    /**
     * Returns true if this integrated server is open to LAN
     */
    public boolean getPublic() {
        return isPublic;
    }

    /**
     * Return whether command blocks are enabled.
     */
    public boolean isCommandBlockEnabled() {
        return true;
    }

    public int getOpPermissionLevel() {
        return 4;
    }

    private void onTick() {
        for (WorldServer worldserver : worldServers) {
            onTick(worldserver);
        }
    }

    public DifficultyInstance getDifficultyAsync(World p_getDifficultyAsync_1_, BlockPos p_getDifficultyAsync_2_) {
        difficultyUpdateWorld = p_getDifficultyAsync_1_;
        difficultyUpdatePos = p_getDifficultyAsync_2_;
        return difficultyLast;
    }

    private void onTick(WorldServer p_onTick_1_) {
        if (!Config.isTimeDefault()) {
            fixWorldTime(p_onTick_1_);
        }

        if (!Config.isWeatherEnabled()) {
            fixWorldWeather(p_onTick_1_);
        }

        if (Config.waterOpacityChanged) {
            Config.waterOpacityChanged = false;
            ClearWater.updateWaterOpacity(Config.getGameSettings(), p_onTick_1_);
        }

        if (difficultyUpdateWorld == p_onTick_1_ && difficultyUpdatePos != null) {
            difficultyLast = p_onTick_1_.getDifficultyForLocation(difficultyUpdatePos);
            difficultyUpdateWorld = null;
            difficultyUpdatePos = null;
        }
    }

    private void fixWorldWeather(WorldServer p_fixWorldWeather_1_) {
        WorldInfo worldinfo = p_fixWorldWeather_1_.getWorldInfo();

        if (worldinfo.isRaining() || worldinfo.isThundering()) {
            worldinfo.setRainTime(0);
            worldinfo.setRaining(false);
            p_fixWorldWeather_1_.setRainStrength(0.0F);
            worldinfo.setThunderTime(0);
            worldinfo.setThundering(false);
            p_fixWorldWeather_1_.setThunderStrength(0.0F);
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, 0.0F));
            getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, 0.0F));
        }
    }

    private void fixWorldTime(WorldServer p_fixWorldTime_1_) {
        WorldInfo worldinfo = p_fixWorldTime_1_.getWorldInfo();

        if (worldinfo.getGameType().getID() == 1) {
            long i = p_fixWorldTime_1_.getWorldTime();
            long j = i % 24000L;

            if (Config.isTimeDayOnly()) {
                if (j <= 1000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 1001L);
                }

                if (j >= 11000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 24001L);
                }
            }

            if (Config.isTimeNightOnly()) {
                if (j <= 14000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 14001L);
                }

                if (j >= 22000L) {
                    p_fixWorldTime_1_.setWorldTime(i - j + 24000L + 14001L);
                }
            }
        }
    }
}
