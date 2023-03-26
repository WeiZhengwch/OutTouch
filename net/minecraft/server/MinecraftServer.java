package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.command.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public abstract class MinecraftServer implements Runnable, ICommandSender, IThreadListener, IPlayerUsage {
    public static final File USER_CACHE_FILE = new File("usercache.json");
    private static final Logger logger = LogManager.getLogger();
    /**
     * Instance of Minecraft Server.
     */
    private static MinecraftServer mcServer;
    public final Profiler theProfiler = new Profiler();
    public final long[] tickTimeArray = new long[100];
    protected final ICommandManager commandManager;
    protected final Proxy serverProxy;
    protected final Queue<FutureTask<?>> futureTaskQueue = Queues.newArrayDeque();
    private final ISaveFormat anvilConverterForAnvilFile;
    /**
     * The PlayerUsageSnooper instance.
     */
    private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("server", this, getCurrentTimeMillis());
    private final File anvilFile;
    private final List<ITickable> playersOnline = Lists.newArrayList();
    private final NetworkSystem networkSystem;
    private final ServerStatusResponse statusResponse = new ServerStatusResponse();
    private final Random random = new Random();
    /**
     * The server's port.
     */
    private final int serverPort = -1;
    private final YggdrasilAuthenticationService authService;
    private final MinecraftSessionService sessionService;
    private final GameProfileRepository profileRepo;
    private final PlayerProfileCache profileCache;
    /**
     * The server world instances.
     */
    public WorldServer[] worldServers;
    /**
     * The task the server is currently working on(and will output on outputPercentRemaining).
     */
    public String currentTask;
    /**
     * The percentage of the current task finished so far.
     */
    public int percentDone;
    /**
     * Stats are [dimension][tick%100] system.nanoTime is stored.
     */
    public long[][] timeOfLastDimensionTick;
    /**
     * The ServerConfigurationManager instance.
     */
    private ServerConfigurationManager serverConfigManager;
    /**
     * Indicates whether the server is running or not. Set to false to initiate a shutdown.
     */
    private boolean serverRunning = true;
    /**
     * Indicates to other classes that the server is safely stopped.
     */
    private boolean serverStopped;
    /**
     * Incremented every tick.
     */
    private int tickCounter;
    /**
     * True if the server is in online mode.
     */
    private boolean onlineMode;
    /**
     * True if the server has animals turned on.
     */
    private boolean canSpawnAnimals;
    private boolean canSpawnNPCs;
    /**
     * Indicates whether PvP is active on the server or not.
     */
    private boolean pvpEnabled;
    /**
     * Determines if flight is allowed or not.
     */
    private boolean allowFlight;
    /**
     * The server MOTD string.
     */
    private String motd;
    /**
     * Maximum build height.
     */
    private int buildLimit;
    private int maxPlayerIdleMinutes;
    private KeyPair serverKeyPair;
    /**
     * Username of the server owner (for integrated servers)
     */
    private String serverOwner;
    private String folderName;
    private String worldName;
    private boolean isDemo;
    private boolean enableBonusChest;
    /**
     * If true, there is no need to save chunks or stop the server, because that is already being done.
     */
    private boolean worldIsBeingDeleted;
    /**
     * The texture pack for the server
     */
    private String resourcePackUrl = "";
    private String resourcePackHash = "";
    private boolean serverIsRunning;
    /**
     * Set when warned for "Can't keep up", which triggers again after 15 seconds.
     */
    private long timeOfLastWarning;
    private String userMessage;
    private boolean startProfiling;
    private boolean isGamemodeForced;
    private long nanoTimeSinceStatusRefresh;
    private Thread serverThread;
    private long currentTime = getCurrentTimeMillis();

    public MinecraftServer(Proxy proxy, File workDir) {
        serverProxy = proxy;
        mcServer = this;
        anvilFile = null;
        networkSystem = null;
        profileCache = new PlayerProfileCache(this, workDir);
        commandManager = null;
        anvilConverterForAnvilFile = null;
        authService = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        sessionService = authService.createMinecraftSessionService();
        profileRepo = authService.createProfileRepository();
    }

    public MinecraftServer(File workDir, Proxy proxy, File profileCacheDir) {
        serverProxy = proxy;
        mcServer = this;
        anvilFile = workDir;
        networkSystem = new NetworkSystem(this);
        profileCache = new PlayerProfileCache(this, profileCacheDir);
        commandManager = createNewCommandManager();
        anvilConverterForAnvilFile = new AnvilSaveConverter(workDir);
        authService = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        sessionService = authService.createMinecraftSessionService();
        profileRepo = authService.createProfileRepository();
    }

    /**
     * Gets mcServer.
     */
    public static MinecraftServer getServer() {
        return mcServer;
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    protected ServerCommandManager createNewCommandManager() {
        return new ServerCommandManager();
    }

    /**
     * Initialises the server and starts it.
     */
    protected abstract boolean startServer();

    protected void convertMapIfNeeded(String worldNameIn) {
        if (getActiveAnvilConverter().isOldMapFormat(worldNameIn)) {
            logger.info("Converting map!");
            setUserMessage("menu.convertingLevel");
            getActiveAnvilConverter().convertMapFormat(worldNameIn, new IProgressUpdate() {
                private long startTime = System.currentTimeMillis();

                public void displaySavingString(String message) {
                }

                public void resetProgressAndMessage(String message) {
                }

                public void setLoadingProgress(int progress) {
                    if (System.currentTimeMillis() - startTime >= 1000L) {
                        startTime = System.currentTimeMillis();
                        logger.info("Converting... " + progress + "%");
                    }
                }

                public void setDoneWorking() {
                }

                public void displayLoadingString(String message) {
                }
            });
        }
    }

    public synchronized String getUserMessage() {
        return userMessage;
    }

    /**
     * Typically "menu.convertingLevel", "menu.loadingLevel" or others.
     */
    protected synchronized void setUserMessage(String message) {
        userMessage = message;
    }

    protected void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String worldNameIn2) {
        convertMapIfNeeded(saveName);
        setUserMessage("menu.loadingLevel");
        worldServers = new WorldServer[3];
        timeOfLastDimensionTick = new long[worldServers.length][100];
        ISaveHandler isavehandler = anvilConverterForAnvilFile.getSaveLoader(saveName, true);
        setResourcePackFromWorld(getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        WorldSettings worldsettings;

        if (worldinfo == null) {
            if (isDemo()) {
                worldsettings = DemoWorldServer.demoWorldSettings;
            } else {
                worldsettings = new WorldSettings(seed, getGameType(), canStructuresSpawn(), isHardcore(), type);
                worldsettings.setWorldName(worldNameIn2);

                if (enableBonusChest) {
                    worldsettings.enableBonusChest();
                }
            }

            worldinfo = new WorldInfo(worldsettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
            worldsettings = new WorldSettings(worldinfo);
        }

        for (int i = 0; i < worldServers.length; ++i) {
            int j = 0;

            if (i == 1) {
                j = -1;
            }

            if (i == 2) {
                j = 1;
            }

            if (i == 0) {
                if (isDemo()) {
                    worldServers[0] = (WorldServer) (new DemoWorldServer(this, isavehandler, worldinfo, j, theProfiler)).init();
                } else {
                    worldServers[0] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, j, theProfiler)).init();
                }

                worldServers[0].initialize(worldsettings);
            } else {
                worldServers[i] = (WorldServer) (new WorldServerMulti(this, isavehandler, j, worldServers[0], theProfiler)).init();
            }

            worldServers[i].addWorldAccess(new WorldManager(this, worldServers[i]));

            if (!isSinglePlayer()) {
                worldServers[i].getWorldInfo().setGameType(getGameType());
            }
        }

        serverConfigManager.setPlayerManager(worldServers);
        setDifficultyForAllWorlds(getDifficulty());
        initialWorldChunkLoad();
    }

    protected void initialWorldChunkLoad() {
        int i = 16;
        int j = 4;
        int k = 192;
        int l = 625;
        int i1 = 0;
        setUserMessage("menu.generatingTerrain");
        int j1 = 0;
        logger.info("Preparing start region for level " + j1);
        WorldServer worldserver = worldServers[j1];
        BlockPos blockpos = worldserver.getSpawnPoint();
        long k1 = getCurrentTimeMillis();

        for (int l1 = -192; l1 <= 192 && isServerRunning(); l1 += 16) {
            for (int i2 = -192; i2 <= 192 && isServerRunning(); i2 += 16) {
                long j2 = getCurrentTimeMillis();

                if (j2 - k1 > 1000L) {
                    outputPercentRemaining("Preparing spawn area", i1 * 100 / 625);
                    k1 = j2;
                }

                ++i1;
                worldserver.theChunkProviderServer.loadChunk(blockpos.getX() + l1 >> 4, blockpos.getZ() + i2 >> 4);
            }
        }

        clearCurrentTask();
    }

    protected void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn) {
        File file1 = new File(saveHandlerIn.getWorldDirectory(), "resources.zip");

        if (file1.isFile()) {
            setResourcePack("level://" + worldNameIn + "/" + file1.getName(), "");
        }
    }

    public abstract boolean canStructuresSpawn();

    public abstract WorldSettings.GameType getGameType();

    /**
     * Sets the game type for all worlds.
     */
    public void setGameType(WorldSettings.GameType gameMode) {
        for (int i = 0; i < worldServers.length; ++i) {
            getServer().worldServers[i].getWorldInfo().setGameType(gameMode);
        }
    }

    /**
     * Get the server's difficulty
     */
    public abstract EnumDifficulty getDifficulty();

    /**
     * Defaults to false.
     */
    public abstract boolean isHardcore();

    public abstract int getOpPermissionLevel();

    /**
     * Get if RCON command events should be broadcast to ops
     */
    public abstract boolean shouldBroadcastRconToOps();

    /**
     * Get if console command events should be broadcast to ops
     */
    public abstract boolean shouldBroadcastConsoleToOps();

    /**
     * Used to display a percent remaining given text and the percentage.
     */
    protected void outputPercentRemaining(String message, int percent) {
        currentTask = message;
        percentDone = percent;
        logger.info(message + ": " + percent + "%");
    }

    /**
     * Set current task to null and set its percentage to 0.
     */
    protected void clearCurrentTask() {
        currentTask = null;
        percentDone = 0;
    }

    /**
     * par1 indicates if a log message should be output.
     */
    protected void saveAllWorlds(boolean dontLog) {
        if (!worldIsBeingDeleted) {
            for (WorldServer worldserver : worldServers) {
                if (worldserver != null) {
                    if (!dontLog) {
                        logger.info("Saving chunks for level '" + worldserver.getWorldInfo().getWorldName() + "'/" + worldserver.provider.getDimensionName());
                    }

                    try {
                        worldserver.saveAllChunks(true, null);
                    } catch (MinecraftException minecraftexception) {
                        logger.warn(minecraftexception.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Saves all necessary data as preparation for stopping the server.
     */
    public void stopServer() {
        if (!worldIsBeingDeleted) {
            logger.info("Stopping server");

            if (getNetworkSystem() != null) {
                getNetworkSystem().terminateEndpoints();
            }

            if (serverConfigManager != null) {
                logger.info("Saving players");
                serverConfigManager.saveAllPlayerData();
                serverConfigManager.removeAllPlayers();
            }

            if (worldServers != null) {
                logger.info("Saving worlds");
                saveAllWorlds(false);

                for (WorldServer worldserver : worldServers) {
                    worldserver.flush();
                }
            }

            if (usageSnooper.isSnooperRunning()) {
                usageSnooper.stopSnooper();
            }
        }
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    /**
     * Sets the serverRunning variable to false, in order to get the server to shut down.
     */
    public void initiateShutdown() {
        serverRunning = false;
    }

    protected void setInstance() {
        mcServer = this;
    }

    public void run() {
        try {
            if (startServer()) {
                currentTime = getCurrentTimeMillis();
                long i = 0L;
                statusResponse.setServerDescription(new ChatComponentText(motd));
                statusResponse.setProtocolVersionInfo(new ServerStatusResponse.MinecraftProtocolVersionIdentifier("1.8.9", 47));
                addFaviconToStatusResponse(statusResponse);

                while (serverRunning) {
                    long k = getCurrentTimeMillis();
                    long j = k - currentTime;

                    if (j > 2000L && currentTime - timeOfLastWarning >= 15000L) {
                        logger.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{j, j / 50L});
                        j = 2000L;
                        timeOfLastWarning = currentTime;
                    }

                    if (j < 0L) {
                        logger.warn("Time ran backwards! Did the system time change?");
                        j = 0L;
                    }

                    i += j;
                    currentTime = k;

                    if (worldServers[0].areAllPlayersAsleep()) {
                        tick();
                        i = 0L;
                    } else {
                        while (i > 50L) {
                            i -= 50L;
                            tick();
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    serverIsRunning = true;
                }
            } else {
                finalTick(null);
            }
        } catch (Throwable throwable1) {
            logger.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport = null;

            if (throwable1 instanceof ReportedException) {
                crashreport = addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
            } else {
                crashreport = addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1)) {
                logger.error("This crash report has been saved to: " + file1.getAbsolutePath());
            } else {
                logger.error("We were unable to save this crash report to disk.");
            }

            finalTick(crashreport);
        } finally {
            try {
                serverStopped = true;
                stopServer();
            } catch (Throwable throwable) {
                logger.error("Exception stopping the server", throwable);
            } finally {
                systemExitNow();
            }
        }
    }

    private void addFaviconToStatusResponse(ServerStatusResponse response) {
        File file1 = getFile("server-icon.png");

        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);
                response.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
            } catch (Exception exception) {
                logger.error("Couldn't load server icon", exception);
            } finally {
                bytebuf.release();
            }
        }
    }

    public File getDataDirectory() {
        return new File(".");
    }

    /**
     * Called on exit from the main run() loop.
     */
    protected void finalTick(CrashReport report) {
    }

    /**
     * Directly calls System.exit(0), instantly killing the program.
     */
    protected void systemExitNow() {
    }

    /**
     * Main function called by run() every loop.
     */
    public void tick() {
        long i = System.nanoTime();
        ++tickCounter;

        if (startProfiling) {
            startProfiling = false;
            theProfiler.profilingEnabled = true;
            theProfiler.clearProfiling();
        }

        theProfiler.startSection("root");
        updateTimeLightAndEntities();

        if (i - nanoTimeSinceStatusRefresh >= 5000000000L) {
            nanoTimeSinceStatusRefresh = i;
            statusResponse.setPlayerCountData(new ServerStatusResponse.PlayerCountData(getMaxPlayers(), getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(getCurrentPlayerCount(), 12)];
            int j = MathHelper.getRandomIntegerInRange(random, 0, getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = serverConfigManager.getPlayerList().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            statusResponse.getPlayerCountData().setPlayers(agameprofile);
        }

        if (tickCounter % 900 == 0) {
            theProfiler.startSection("save");
            serverConfigManager.saveAllPlayerData();
            saveAllWorlds(true);
            theProfiler.endSection();
        }

        theProfiler.startSection("tallying");
        tickTimeArray[tickCounter % 100] = System.nanoTime() - i;
        theProfiler.endSection();
        theProfiler.startSection("snooper");

        if (!usageSnooper.isSnooperRunning() && tickCounter > 100) {
            usageSnooper.startSnooper();
        }

        if (tickCounter % 6000 == 0) {
            usageSnooper.addMemoryStatsToSnooper();
        }

        theProfiler.endSection();
        theProfiler.endSection();
    }

    public void updateTimeLightAndEntities() {
        theProfiler.startSection("jobs");

        synchronized (futureTaskQueue) {
            while (!futureTaskQueue.isEmpty()) {
                Util.runTask((FutureTask) futureTaskQueue.poll(), logger);
            }
        }

        theProfiler.endStartSection("levels");

        for (int j = 0; j < worldServers.length; ++j) {
            long i = System.nanoTime();

            if (j == 0 || getAllowNether()) {
                WorldServer worldserver = worldServers[j];
                theProfiler.startSection(worldserver.getWorldInfo().getWorldName());

                if (tickCounter % 20 == 0) {
                    theProfiler.startSection("timeSync");
                    serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimensionId());
                    theProfiler.endSection();
                }

                theProfiler.startSection("tick");

                try {
                    worldserver.tick();
                } catch (Throwable throwable1) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                    worldserver.addWorldInfoToCrashReport(crashreport);
                    throw new ReportedException(crashreport);
                }

                try {
                    worldserver.updateEntities();
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                    worldserver.addWorldInfoToCrashReport(crashreport1);
                    throw new ReportedException(crashreport1);
                }

                theProfiler.endSection();
                theProfiler.startSection("tracker");
                worldserver.getEntityTracker().updateTrackedEntities();
                theProfiler.endSection();
                theProfiler.endSection();
            }

            timeOfLastDimensionTick[j][tickCounter % 100] = System.nanoTime() - i;
        }

        theProfiler.endStartSection("connection");
        getNetworkSystem().networkTick();
        theProfiler.endStartSection("players");
        serverConfigManager.onTick();
        theProfiler.endStartSection("tickables");

        for (ITickable iTickable : playersOnline) {
            iTickable.update();
        }

        theProfiler.endSection();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void startServerThread() {
        serverThread = new Thread(this, "Server thread");
        serverThread.start();
    }

    /**
     * Returns a File object from the specified string.
     */
    public File getFile(String fileName) {
        return new File(getDataDirectory(), fileName);
    }

    /**
     * Logs the message with a level of WARN.
     */
    public void logWarning(String msg) {
        logger.warn(msg);
    }

    /**
     * Gets the worldServer by the given dimension.
     */
    public WorldServer worldServerForDimension(int dimension) {
        return dimension == -1 ? worldServers[1] : (dimension == 1 ? worldServers[2] : worldServers[0]);
    }

    /**
     * Returns the server's Minecraft version as string.
     */
    public String getMinecraftVersion() {
        return "1.8.9";
    }

    /**
     * Returns the number of players currently on the server.
     */
    public int getCurrentPlayerCount() {
        return serverConfigManager.getCurrentPlayerCount();
    }

    /**
     * Returns the maximum number of players allowed on the server.
     */
    public int getMaxPlayers() {
        return serverConfigManager.getMaxPlayers();
    }

    /**
     * Returns an array of the usernames of all the connected players.
     */
    public String[] getAllUsernames() {
        return serverConfigManager.getAllUsernames();
    }

    /**
     * Returns an array of the GameProfiles of all the connected players
     */
    public GameProfile[] getGameProfiles() {
        return serverConfigManager.getAllProfiles();
    }

    public String getServerModName() {
        return "vanilla";
    }

    /**
     * Adds the server info, including from theWorldServer, to the crash report.
     */
    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report.getCategory().addCrashSectionCallable("Profiler Position", () -> theProfiler.profilingEnabled ? theProfiler.getNameOfLastSection() : "N/A (disabled)");

        if (serverConfigManager != null) {
            report.getCategory().addCrashSectionCallable("Player Count", () -> serverConfigManager.getCurrentPlayerCount() + " / " + serverConfigManager.getMaxPlayers() + "; " + serverConfigManager.getPlayerList());
        }

        return report;
    }

    public List<String> getTabCompletions(ICommandSender sender, String input, BlockPos pos) {
        List<String> list = Lists.newArrayList();

        if (input.startsWith("/")) {
            input = input.substring(1);
            boolean flag = !input.contains(" ");
            List<String> list1 = commandManager.getTabCompletionOptions(sender, input, pos);

            if (list1 != null) {
                for (String s2 : list1) {
                    if (flag) {
                        list.add("/" + s2);
                    } else {
                        list.add(s2);
                    }
                }
            }

            return list;
        } else {
            String[] astring = input.split(" ", -1);
            String s = astring[astring.length - 1];

            for (String s1 : serverConfigManager.getAllUsernames()) {
                if (CommandBase.doesStringStartWith(s, s1)) {
                    list.add(s1);
                }
            }

            return list;
        }
    }

    public boolean isAnvilFileSet() {
        return anvilFile != null;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return "Server";
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(IChatComponent component) {
        logger.info(component.getUnformattedText());
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return true;
    }

    public ICommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Gets KeyPair instanced in MinecraftServer.
     */
    public KeyPair getKeyPair() {
        return serverKeyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        serverKeyPair = keyPair;
    }

    /**
     * Returns the username of the server owner (for integrated servers)
     */
    public String getServerOwner() {
        return serverOwner;
    }

    /**
     * Sets the username of the owner of this server (in the case of an integrated server)
     */
    public void setServerOwner(String owner) {
        serverOwner = owner;
    }

    public boolean isSinglePlayer() {
        return serverOwner != null;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String name) {
        folderName = name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String p_71246_1_) {
        worldName = p_71246_1_;
    }

    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        for (World world : worldServers) {
            if (world != null) {
                if (world.getWorldInfo().isHardcoreModeEnabled()) {
                    world.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
                    world.setAllowedSpawnTypes(true, true);
                } else if (isSinglePlayer()) {
                    world.getWorldInfo().setDifficulty(difficulty);
                    world.setAllowedSpawnTypes(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    world.getWorldInfo().setDifficulty(difficulty);
                    world.setAllowedSpawnTypes(allowSpawnMonsters(), canSpawnAnimals);
                }
            }
        }
    }

    protected boolean allowSpawnMonsters() {
        return true;
    }

    /**
     * Gets whether this is a demo or not.
     */
    public boolean isDemo() {
        return isDemo;
    }

    /**
     * Sets whether this is a demo or not.
     */
    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public void canCreateBonusChest(boolean enable) {
        enableBonusChest = enable;
    }

    public ISaveFormat getActiveAnvilConverter() {
        return anvilConverterForAnvilFile;
    }

    /**
     * WARNING : directly calls
     * getActiveAnvilConverter().deleteWorldDirectory(theWorldServer[0].getSaveHandler().getWorldDirectoryName());
     */
    public void deleteWorldAndStopServer() {
        worldIsBeingDeleted = true;
        getActiveAnvilConverter().flushCache();

        for (WorldServer worldserver : worldServers) {
            if (worldserver != null) {
                worldserver.flush();
            }
        }

        getActiveAnvilConverter().deleteWorldDirectory(worldServers[0].getSaveHandler().getWorldDirectoryName());
        initiateShutdown();
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public String getResourcePackHash() {
        return resourcePackHash;
    }

    public void setResourcePack(String url, String hash) {
        resourcePackUrl = url;
        resourcePackHash = hash;
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.addClientStat("whitelist_enabled", Boolean.FALSE);
        playerSnooper.addClientStat("whitelist_count", 0);

        if (serverConfigManager != null) {
            playerSnooper.addClientStat("players_current", getCurrentPlayerCount());
            playerSnooper.addClientStat("players_max", getMaxPlayers());
            playerSnooper.addClientStat("players_seen", serverConfigManager.getAvailablePlayerDat().length);
        }

        playerSnooper.addClientStat("uses_auth", onlineMode);
        playerSnooper.addClientStat("gui_state", getGuiEnabled() ? "enabled" : "disabled");
        playerSnooper.addClientStat("run_time", (getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
        playerSnooper.addClientStat("avg_tick_ms", (int) (MathHelper.average(tickTimeArray) * 1.0E-6D));
        int i = 0;

        if (worldServers != null) {
            for (WorldServer worldServer : worldServers) {
                if (worldServer != null) {
                    WorldServer worldserver = worldServer;
                    WorldInfo worldinfo = worldserver.getWorldInfo();
                    playerSnooper.addClientStat("world[" + i + "][dimension]", worldserver.provider.getDimensionId());
                    playerSnooper.addClientStat("world[" + i + "][mode]", worldinfo.getGameType());
                    playerSnooper.addClientStat("world[" + i + "][difficulty]", worldserver.getDifficulty());
                    playerSnooper.addClientStat("world[" + i + "][hardcore]", worldinfo.isHardcoreModeEnabled());
                    playerSnooper.addClientStat("world[" + i + "][generator_name]", worldinfo.getTerrainType().getWorldTypeName());
                    playerSnooper.addClientStat("world[" + i + "][generator_version]", worldinfo.getTerrainType().getGeneratorVersion());
                    playerSnooper.addClientStat("world[" + i + "][height]", buildLimit);
                    playerSnooper.addClientStat("world[" + i + "][chunks_loaded]", worldserver.getChunkProvider().getLoadedChunkCount());
                    ++i;
                }
            }
        }

        playerSnooper.addClientStat("worlds", i);
    }

    public void addServerTypeToSnooper(PlayerUsageSnooper playerSnooper) {
        playerSnooper.addStatToSnooper("singleplayer", isSinglePlayer());
        playerSnooper.addStatToSnooper("server_brand", getServerModName());
        playerSnooper.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        playerSnooper.addStatToSnooper("dedicated", isDedicatedServer());
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled() {
        return true;
    }

    public abstract boolean isDedicatedServer();

    public boolean isServerInOnlineMode() {
        return onlineMode;
    }

    public void setOnlineMode(boolean online) {
        onlineMode = online;
    }

    public boolean getCanSpawnAnimals() {
        return canSpawnAnimals;
    }

    public void setCanSpawnAnimals(boolean spawnAnimals) {
        canSpawnAnimals = spawnAnimals;
    }

    public boolean getCanSpawnNPCs() {
        return canSpawnNPCs;
    }

    public void setCanSpawnNPCs(boolean spawnNpcs) {
        canSpawnNPCs = spawnNpcs;
    }

    /**
     * Get if native transport should be used. Native transport means linux server performance improvements and
     * optimized packet sending/receiving on linux
     */
    public abstract boolean shouldUseNativeTransport();

    public boolean isPVPEnabled() {
        return pvpEnabled;
    }

    public void setAllowPvp(boolean allowPvp) {
        pvpEnabled = allowPvp;
    }

    public boolean isFlightAllowed() {
        return allowFlight;
    }

    public void setAllowFlight(boolean allow) {
        allowFlight = allow;
    }

    /**
     * Return whether command blocks are enabled.
     */
    public abstract boolean isCommandBlockEnabled();

    public String getMOTD() {
        return motd;
    }

    public void setMOTD(String motdIn) {
        motd = motdIn;
    }

    public int getBuildLimit() {
        return buildLimit;
    }

    public void setBuildLimit(int maxBuildHeight) {
        buildLimit = maxBuildHeight;
    }

    public boolean isServerStopped() {
        return serverStopped;
    }

    public ServerConfigurationManager getConfigurationManager() {
        return serverConfigManager;
    }

    public void setConfigManager(ServerConfigurationManager configManager) {
        serverConfigManager = configManager;
    }

    public NetworkSystem getNetworkSystem() {
        return networkSystem;
    }

    public boolean serverIsInRunLoop() {
        return serverIsRunning;
    }

    public boolean getGuiEnabled() {
        return false;
    }

    /**
     * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
     */
    public abstract String shareToLAN(WorldSettings.GameType type, boolean allowCheats);

    public int getTickCounter() {
        return tickCounter;
    }

    public void enableProfiling() {
        startProfiling = true;
    }

    public PlayerUsageSnooper getPlayerUsageSnooper() {
        return usageSnooper;
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition() {
        return BlockPos.ORIGIN;
    }

    /**
     * Get the position vector. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return 0.0D,
     * 0.0D, 0.0D
     */
    public Vec3 getPositionVector() {
        return new Vec3(0.0D, 0.0D, 0.0D);
    }

    /**
     * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the overworld
     */
    public World getEntityWorld() {
        return worldServers[0];
    }

    /**
     * Returns the entity associated with the command sender. MAY BE NULL!
     */
    public Entity getCommandSenderEntity() {
        return null;
    }

    /**
     * Return the spawn protection area's size.
     */
    public int getSpawnProtectionSize() {
        return 16;
    }

    public boolean isBlockProtected(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        return false;
    }

    public boolean getForceGamemode() {
        return isGamemodeForced;
    }

    public Proxy getServerProxy() {
        return serverProxy;
    }

    public int getMaxPlayerIdleMinutes() {
        return maxPlayerIdleMinutes;
    }

    public void setPlayerIdleTimeout(int idleTimeout) {
        maxPlayerIdleMinutes = idleTimeout;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName() {
        return new ChatComponentText(getName());
    }

    public boolean isAnnouncingPlayerAchievements() {
        return true;
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return sessionService;
    }

    public GameProfileRepository getGameProfileRepository() {
        return profileRepo;
    }

    public PlayerProfileCache getPlayerProfileCache() {
        return profileCache;
    }

    public ServerStatusResponse getServerStatusResponse() {
        return statusResponse;
    }

    public void refreshStatusNextTick() {
        nanoTimeSinceStatusRefresh = 0L;
    }

    public Entity getEntityFromUuid(UUID uuid) {
        for (WorldServer worldserver : worldServers) {
            if (worldserver != null) {
                Entity entity = worldserver.getEntityFromUuid(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    /**
     * Returns true if the command sender should be sent feedback about executed commands
     */
    public boolean sendCommandFeedback() {
        return getServer().worldServers[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    public void setCommandStat(CommandResultStats.Type type, int amount) {
    }

    public int getMaxWorldSize() {
        return 29999984;
    }

    public <V> ListenableFuture<V> callFromMainThread(Callable<V> callable) {
        Validate.notNull(callable);

        if (!isCallingFromMinecraftThread() && !isServerStopped()) {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callable);

            synchronized (futureTaskQueue) {
                futureTaskQueue.add(listenablefuturetask);
                return listenablefuturetask;
            }
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        Validate.notNull(runnableToSchedule);
        return callFromMainThread(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == serverThread;
    }

    /**
     * The compression treshold. If the packet is larger than the specified amount of bytes, it will be compressed
     */
    public int getNetworkCompressionTreshold() {
        return 256;
    }
}
