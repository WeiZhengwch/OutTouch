package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.optifine.*;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.util.KeyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class GameSettings {
    public static final int DEFAULT = 0;
    public static final int OFF = 3;
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };
    /**
     * GUI scale values
     */
    private static final String[] GUISCALES = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[]{"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[]{"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] STREAM_COMPRESSIONS = new String[]{"options.stream.compression.low", "options.stream.compression.medium", "options.stream.compression.high"};
    private static final String[] STREAM_CHAT_MODES = new String[]{"options.stream.chat.enabled.streaming", "options.stream.chat.enabled.always", "options.stream.chat.enabled.never"};
    private static final String[] STREAM_CHAT_FILTER_MODES = new String[]{"options.stream.chat.userFilter.all", "options.stream.chat.userFilter.subs", "options.stream.chat.userFilter.mods"};
    private static final String[] STREAM_MIC_MODES = new String[]{"options.stream.mic_toggle.mute", "options.stream.mic_toggle.talk"};
    private static final String[] CLOUDS_TYPES = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
    private static final int[] OF_TREES_VALUES = new int[]{0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[]{3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    private final Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;
    /**
     * Clouds flag
     */
    public int clouds = 2;
    public boolean fancyGraphics = true;
    /**
     * Smooth Lighting
     */
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = false;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo = false;
    public boolean hideServerAddress;
    public float combatrange;
    public float velocityhori;
    public float velocityvert;
    public int fastplace;
    public int autoclicker;
    public boolean nametag;
    public boolean autogg;
    public boolean viewclip;
    public boolean blockinghit;
    public boolean noslow;
    public boolean eagle;
    public boolean autotool;
    public boolean moduleslist;

    public boolean rawmouseinput;
    public int chunkupdateslimit;
    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;
    /**
     * Whether to pause when the game loses focus, toggled by F3+P
     */
    public boolean pauseOnLostFocus = true;
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    public float streamBytesPerPixel = 0.5F;
    public float streamMicVolume = 1.0F;
    public float streamGameVolume = 1.0F;
    public float streamKbps = 0.5412844F;
    public float streamFps = 0.31690142F;
    public int streamCompression = 1;
    public boolean streamSendMetadata = true;
    public String streamPreferredServer = "";
    public int streamChatEnabled = 0;
    public int streamChatUserFilter = 0;
    public int streamMicToggleBehavior = 0;
    public boolean useNativeTransport = true;
    public boolean entityShadows = true;
    public boolean realmsNotifications = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding keyBindStreamStartStop = new KeyBinding("key.streamStartStop", 64, "key.categories.stream");
    public KeyBinding keyBindStreamPauseUnpause = new KeyBinding("key.streamPauseUnpause", 65, "key.categories.stream");
    public KeyBinding keyBindStreamCommercials = new KeyBinding("key.streamCommercial", 0, "key.categories.stream");
    public KeyBinding keyBindStreamToggleMic = new KeyBinding("key.streamToggleMic", 0, "key.categories.stream");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;
    /**
     * true if debug info should be displayed instead of version
     */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean showLagometer;
    /**
     * The lastServer string.
     */
    public String lastServer;
    /**
     * Smooth Camera Toggle
     */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;
    /**
     * GUI scale
     */
    public int guiScale;
    /**
     * Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal
     */
    public int particleSetting;
    /**
     * Game settings language
     */
    public String language;
    public boolean forceUnicodeFont;
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public boolean ofRenderRegions = false;
    public boolean ofSmartAnimations = false;
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomEntities = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofEmissiveTextures = true;
    public boolean ofFastMath = false;
    public boolean ofFastRender = false;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public boolean ofAlternateBlocks = true;
    public int ofDynamicLights = 3;
    public boolean ofCustomEntityModels = true;
    public boolean ofCustomGuis = true;
    public boolean ofShowGlErrors = true;
    public int ofScreenshotSize = 1;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public KeyBinding ofKeyBindZoom;
    protected Minecraft mc;
    private File optionsFile;
    private File optionsFileOF;

    public GameSettings(Minecraft mcIn, File optionsFileIn) {
        keyBindings = ArrayUtils.addAll(new KeyBinding[]{keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        velocityvert = 100.0F;
        velocityhori = 100.0F;
        fastplace = 5;
        rawmouseinput = false;
        chunkupdateslimit = 250;
        autoclicker = 20;
        language = "en_US";
        forceUnicodeFont = false;
        mc = mcIn;
        optionsFile = new File(optionsFileIn, "options.txt");

        if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
            long i = 1000000L;

            if (Runtime.getRuntime().maxMemory() >= 1500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(48.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 2500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(64.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 5500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(80.0F);
            }
        } else {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        optionsFileOF = new File(optionsFileIn, "optionsof.txt");
        limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        ofKeyBindZoom = new KeyBinding("of.key.zoom", 46, "key.categories.misc");
        keyBindings = ArrayUtils.add(keyBindings, ofKeyBindZoom);
        KeyUtils.fixKeyConflicts(keyBindings, new KeyBinding[]{ofKeyBindZoom});
        renderDistanceChunks = 8;
        loadOptions();
        Config.initGameSettings(this);
    }

    public GameSettings() {
        keyBindings = ArrayUtils.addAll(new KeyBinding[]{keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     *
     * @param key The key to display
     */
    public static String getKeyDisplayString(int key) {
        return key < 0 ? I18n.format("key.mouseButton", key + 101) : (key < 256 ? Keyboard.getKeyName(key) : String.format("%c", (char) (key - 256)).toUpperCase());
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     *
     * @param key The key tested
     */
    public static boolean isKeyDown(KeyBinding key) {
        return key.getKeyCode() != 0 && (key.getKeyCode() < 0 ? Mouse.isButtonDown(key.getKeyCode() + 100) : Keyboard.isKeyDown(key.getKeyCode()));
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     *
     * @param strArray The array of string containing the string to translate
     * @param index    The index in the array of the string to translate
     */
    private static String getTranslation(String[] strArray, int index) {
        if (index < 0 || index >= strArray.length) {
            index = 0;
        }

        return I18n.format(strArray[index]);
    }

    private static int nextValue(int p_nextValue_0_, int[] p_nextValue_1_) {
        int i = indexOf(p_nextValue_0_, p_nextValue_1_);

        if (i < 0) {
            return p_nextValue_1_[0];
        } else {
            ++i;

            if (i >= p_nextValue_1_.length) {
                i = 0;
            }

            return p_nextValue_1_[i];
        }
    }

    private static int limit(int p_limit_0_, int[] p_limit_1_) {
        int i = indexOf(p_limit_0_, p_limit_1_);
        return i < 0 ? p_limit_1_[0] : p_limit_0_;
    }

    private static int indexOf(int p_indexOf_0_, int[] p_indexOf_1_) {
        for (int i = 0; i < p_indexOf_1_.length; ++i) {
            if (p_indexOf_1_[i] == p_indexOf_0_) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Sets a key binding and then saves all settings.
     *
     * @param key     The key that the option will be set
     * @param keyCode The option (keycode) to set.
     */
    public void setOptionKeyBinding(KeyBinding key, int keyCode) {
        key.setKeyCode(keyCode);
        saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     *
     * @param settingsOption The option to set to a value
     * @param value          The value that the option will take
     */
    public void setOptionFloatValue(GameSettings.Options settingsOption, float value) {
        setOptionFloatValueOF(settingsOption, value);

        switch (settingsOption) {
            case CLIENT_COMBAT_FASTPLACE -> fastplace = (int) value;
            case CLIENT_COMBAT_AUTOCLICKER -> autoclicker = (int) value;
            case CLIENT_PERFORMACNE_CHUNK_UPDATES_LIMIT -> chunkupdateslimit = (int) value;
            case CLIENT_COMBAT_REACH -> combatrange = value;
            case CLIENT_COMBAT_VELOCITY_HORI -> velocityhori = value;
            case CLIENT_COMBAT_VELOCITY_VERT -> velocityvert = value;
            case SENSITIVITY -> mouseSensitivity = value;
            case FOV -> fovSetting = value;
            case GAMMA -> gammaSetting = value;
            case FRAMERATE_LIMIT -> {
                limitFramerate = (int) value;
                enableVsync = false;

                if (limitFramerate <= 0) {
                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                    enableVsync = true;
                }

                updateVSync();
            }
            case CHAT_OPACITY -> {
                chatOpacity = value;
                mc.ingameGUI.getChatGUI().refreshChat();
            }
            case CHAT_HEIGHT_FOCUSED -> {
                chatHeightFocused = value;
                mc.ingameGUI.getChatGUI().refreshChat();
            }
            case CHAT_HEIGHT_UNFOCUSED -> {
                chatHeightUnfocused = value;
                mc.ingameGUI.getChatGUI().refreshChat();
            }
            case CHAT_WIDTH -> {
                chatWidth = value;
                mc.ingameGUI.getChatGUI().refreshChat();
            }
            case CHAT_SCALE -> {
                chatScale = value;
                mc.ingameGUI.getChatGUI().refreshChat();
            }
            case MIPMAP_LEVELS -> {
                int i = mipmapLevels;
                mipmapLevels = (int) value;

                if ((float) i != value) {
                    mc.getTextureMapBlocks().setMipmapLevels(mipmapLevels);
                    mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                    mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmapLevels > 0);
                    mc.scheduleResourcesRefresh();
                }
            }
            case BLOCK_ALTERNATIVES -> {
                allowBlockAlternatives = !allowBlockAlternatives;
                mc.renderGlobal.loadRenderers();
            }
            case RENDER_DISTANCE -> {
                renderDistanceChunks = (int) value;
                mc.renderGlobal.setDisplayListEntitiesDirty();
            }
            case STREAM_BYTES_PER_PIXEL -> streamBytesPerPixel = value;
            case STREAM_VOLUME_MIC -> streamMicVolume = value;
            case STREAM_VOLUME_SYSTEM -> streamGameVolume = value;
            case STREAM_KBPS -> streamKbps = value;
            case STREAM_FPS -> streamFps = value;
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     *
     * @param settingsOption The option to set to a value
     * @param value          The value that the option will take
     */
    public void setOptionValue(GameSettings.Options settingsOption, int value) {
        setOptionValueOF(settingsOption, value);

        switch (settingsOption) {
            case CLIENT_RENDER_NAMETAG -> nametag = !nametag;
            case CLIENT_RENDER_VIEWCLIP -> viewclip = !viewclip;
            case CLIENT_PLAYER_AUTOGG -> autogg = !autogg;
            case CLIENT_MOVEMENT_EAGLE -> eagle = !eagle;
            case CLIENT_PLAYER_AUTOTOOL -> autotool = !autotool;
            case CLIENT_RENDER_MODULESLIST -> moduleslist = !moduleslist;
            case CLIENT_COMBAT_BLOCKINGHIT -> blockinghit = !blockinghit;
            case CLIENT_MOVEMENT_NOSLOW -> noslow = !noslow;
            case CLIENT_MISC_RAWMOUSEINPUT -> rawmouseinput = !rawmouseinput;
            case INVERT_MOUSE -> invertMouse = !invertMouse;
            case GUI_SCALE -> {
                guiScale += value;

                if (GuiScreen.isShiftKeyDown()) {
                    guiScale = 0;
                }

                DisplayMode displaymode = Config.getLargestDisplayMode();
                int i = displaymode.getWidth() / 320;
                int j = displaymode.getHeight() / 240;
                int k = Math.min(i, j);

                if (guiScale < 0) {
                    guiScale = k - 1;
                }

                if (mc.isUnicode() && guiScale % 2 != 0) {
                    guiScale += value;
                }

                if (guiScale < 0 || guiScale >= k) {
                    guiScale = 0;
                }
            }
            case RENDER_DISTANCE ->
                    setOptionFloatValue(Options.RENDER_DISTANCE, MathHelper.clamp_float((float) (renderDistanceChunks + value), Options.RENDER_DISTANCE.getValueMin(), Options.RENDER_DISTANCE.getValueMax()));
            case PARTICLES -> particleSetting = (particleSetting + value) % 3;
            case VIEW_BOBBING -> viewBobbing = !viewBobbing;
            case RENDER_CLOUDS -> clouds = (clouds + value) % 3;
            case FORCE_UNICODE_FONT -> {
                forceUnicodeFont = !forceUnicodeFont;
                mc.fontRendererObj.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
            }
            case FBO_ENABLE -> fboEnable = !fboEnable;
            case ANAGLYPH -> {
                if (!anaglyph && Config.isShaders()) {
                    Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                    return;
                }

                anaglyph = !anaglyph;
                mc.refreshResources();
            }
            case GRAPHICS -> {
                fancyGraphics = !fancyGraphics;
                updateRenderClouds();
                mc.renderGlobal.loadRenderers();
            }
            case AMBIENT_OCCLUSION -> {
                ambientOcclusion = (ambientOcclusion + value) % 3;
                mc.renderGlobal.loadRenderers();
            }
            case CHAT_VISIBILITY ->
                    chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + value) % 3);
            case STREAM_COMPRESSION -> streamCompression = (streamCompression + value) % 3;
            case STREAM_SEND_METADATA -> streamSendMetadata = !streamSendMetadata;
            case STREAM_CHAT_ENABLED -> streamChatEnabled = (streamChatEnabled + value) % 3;
            case STREAM_CHAT_USER_FILTER -> streamChatUserFilter = (streamChatUserFilter + value) % 3;
            case STREAM_MIC_TOGGLE_BEHAVIOR -> streamMicToggleBehavior = (streamMicToggleBehavior + value) % 2;
            case CHAT_COLOR -> chatColours = !chatColours;
            case CHAT_LINKS -> chatLinks = !chatLinks;
            case CHAT_LINKS_PROMPT -> chatLinksPrompt = !chatLinksPrompt;
            case SNOOPER_ENABLED -> snooperEnabled = !snooperEnabled;
            case TOUCHSCREEN -> touchscreen = !touchscreen;
            case USE_FULLSCREEN -> {
                fullScreen = !fullScreen;
                mc.toggleFullscreen();
            }
            case ENABLE_VSYNC -> {
                enableVsync = !enableVsync;
                Display.setVSyncEnabled(enableVsync);
            }
            case USE_VBO -> {
                useVbo = !useVbo;
                mc.renderGlobal.loadRenderers();
            }
            case BLOCK_ALTERNATIVES -> {
                allowBlockAlternatives = !allowBlockAlternatives;
                mc.renderGlobal.loadRenderers();
            }
            case REDUCED_DEBUG_INFO -> reducedDebugInfo = !reducedDebugInfo;
            case ENTITY_SHADOWS -> entityShadows = !entityShadows;
            case REALMS_NOTIFICATIONS -> realmsNotifications = !realmsNotifications;
        }
//        new MainClient().loader();
        saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options settingOption) {
        float f = getOptionFloatValueOF(settingOption);
        return f != Float.MAX_VALUE ? f : (settingOption == Options.FOV ? fovSetting : (settingOption == Options.GAMMA ? gammaSetting : (settingOption == Options.SATURATION ? saturation : (settingOption == Options.SENSITIVITY ? mouseSensitivity : (settingOption == Options.CHAT_OPACITY ? chatOpacity : (settingOption == Options.CHAT_HEIGHT_FOCUSED ? chatHeightFocused : (settingOption == Options.CHAT_HEIGHT_UNFOCUSED ? chatHeightUnfocused : (settingOption == Options.CHAT_SCALE ? chatScale : (settingOption == Options.CHAT_WIDTH ? chatWidth : (settingOption == Options.FRAMERATE_LIMIT ? (float) limitFramerate : (settingOption == Options.MIPMAP_LEVELS ? (float) mipmapLevels : (settingOption == Options.RENDER_DISTANCE ? (float) renderDistanceChunks : (settingOption == Options.CLIENT_PERFORMACNE_CHUNK_UPDATES_LIMIT ? chunkupdateslimit : (settingOption == Options.CLIENT_COMBAT_VELOCITY_VERT ? velocityvert : (settingOption == Options.CLIENT_COMBAT_FASTPLACE ? fastplace : (settingOption == Options.CLIENT_COMBAT_AUTOCLICKER ? autoclicker : (settingOption == Options.CLIENT_COMBAT_VELOCITY_HORI ? velocityhori : (settingOption == Options.CLIENT_COMBAT_REACH ? combatrange : 3.0F))))))))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options settingOption) {
        return switch (settingOption) {
            case CLIENT_RENDER_NAMETAG -> nametag;
            case CLIENT_RENDER_VIEWCLIP -> viewclip;
            case CLIENT_PLAYER_AUTOGG -> autogg;
            case CLIENT_PLAYER_AUTOTOOL -> autotool;
            case CLIENT_RENDER_MODULESLIST -> moduleslist;
            case CLIENT_MISC_RAWMOUSEINPUT -> rawmouseinput;
            case CLIENT_COMBAT_BLOCKINGHIT -> blockinghit;
            case CLIENT_MOVEMENT_NOSLOW -> noslow;
            case CLIENT_MOVEMENT_EAGLE -> eagle;
            case INVERT_MOUSE -> invertMouse;
            case VIEW_BOBBING -> viewBobbing;
            case ANAGLYPH -> anaglyph;
            case FBO_ENABLE -> fboEnable;
            case CHAT_COLOR -> chatColours;
            case CHAT_LINKS -> chatLinks;
            case CHAT_LINKS_PROMPT -> chatLinksPrompt;
            case SNOOPER_ENABLED -> snooperEnabled;
            case USE_FULLSCREEN -> fullScreen;
            case ENABLE_VSYNC -> enableVsync;
            case USE_VBO -> useVbo;
            case TOUCHSCREEN -> touchscreen;
            case STREAM_SEND_METADATA -> streamSendMetadata;
            case FORCE_UNICODE_FONT -> forceUnicodeFont;
            case BLOCK_ALTERNATIVES -> allowBlockAlternatives;
            case REDUCED_DEBUG_INFO -> reducedDebugInfo;
            case ENTITY_SHADOWS -> entityShadows;
            case REALMS_NOTIFICATIONS -> realmsNotifications;
            default -> false;
        };
    }

    /**
     * Gets a key binding.
     *
     * @param settingOption The KeyBinding is generated from this option
     */
    public String getKeyBinding(GameSettings.Options settingOption) {
        String s = getKeyBindingOF(settingOption);

        if (s != null) {
            return s;
        } else {
            String s1 = I18n.format(settingOption.getEnumString()) + ": ";

            if (settingOption.getEnumFloat()) {
                float f1 = getOptionFloatValue(settingOption);
                float f = settingOption.normalizeValue(f1);
                return settingOption == GameSettings.Options.MIPMAP_LEVELS && (double) f1 >= 4.0D ? s1 + Lang.get("of.general.max") : (settingOption == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s1 + I18n.format("options.sensitivity.min") : (f == 1.0F ? s1 + I18n.format("options.sensitivity.max") : s1 + (int) (f * 200.0F) + "%")) : (settingOption == GameSettings.Options.FOV ? (f1 == 70.0F ? s1 + I18n.format("options.fov.min") : (f1 == 110.0F ? s1 + I18n.format("options.fov.max") : s1 + (int) f1)) : (settingOption == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == settingOption.valueMax ? s1 + I18n.format("options.framerateLimit.max") : s1 + (int) f1 + " fps") : (settingOption == GameSettings.Options.RENDER_CLOUDS ? (f1 == settingOption.valueMin ? s1 + I18n.format("options.cloudHeight.min") : s1 + ((int) f1 + 128)) : (settingOption == GameSettings.Options.GAMMA ? (f == 0.0F ? s1 + I18n.format("options.gamma.min") : (f == 1.0F ? s1 + I18n.format("options.gamma.max") : s1 + "+" + (int) (f * 100.0F) + "%")) : (settingOption == GameSettings.Options.SATURATION ? s1 + (int) (f * 400.0F) + "%" : (settingOption == GameSettings.Options.CHAT_OPACITY ? s1 + (int) (f * 90.0F + 10.0F) + "%" : (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_WIDTH ? s1 + GuiNewChat.calculateChatboxWidth(f) + "px" : (settingOption == GameSettings.Options.RENDER_DISTANCE ? s1 + (int) f1 + " chunks" : (settingOption == Options.CLIENT_COMBAT_FASTPLACE ? s1 + (int) f1 + " ticks" : (settingOption == Options.CLIENT_COMBAT_AUTOCLICKER ? s1 + (int) f1 + " cps" : (settingOption == Options.CLIENT_PERFORMACNE_CHUNK_UPDATES_LIMIT ? s1 + (int) f1 + " Chunks" : (settingOption == Options.CLIENT_COMBAT_REACH ? s1 + String.format("%.2f ", f1) + "blocks" : (settingOption == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s1 + I18n.format("options.off") : s1 + (int) f1) : ((f == 0.0F && (settingOption != Options.CLIENT_COMBAT_VELOCITY_HORI && settingOption != Options.CLIENT_COMBAT_VELOCITY_VERT)) ? s1 + I18n.format("options.off") : s1 + (int) (f * 100.0F) + "%")))))))))))))))));
            } else if (settingOption.getEnumBoolean()) {
                boolean flag = getOptionOrdinalValue(settingOption);
                return flag ? s1 + I18n.format("options.on") : s1 + I18n.format("options.off");
            } else if (settingOption == GameSettings.Options.GUI_SCALE) {
                return guiScale >= GUISCALES.length ? s1 + guiScale + "x" : s1 + getTranslation(GUISCALES, guiScale);
            } else if (settingOption == GameSettings.Options.CHAT_VISIBILITY) {
                return s1 + I18n.format(chatVisibility.getResourceKey());
            } else if (settingOption == GameSettings.Options.PARTICLES) {
                return s1 + getTranslation(PARTICLES, particleSetting);
            } else if (settingOption == GameSettings.Options.AMBIENT_OCCLUSION) {
                return s1 + getTranslation(AMBIENT_OCCLUSIONS, ambientOcclusion);
            } else if (settingOption == GameSettings.Options.STREAM_COMPRESSION) {
                return s1 + getTranslation(STREAM_COMPRESSIONS, streamCompression);
            } else if (settingOption == GameSettings.Options.STREAM_CHAT_ENABLED) {
                return s1 + getTranslation(STREAM_CHAT_MODES, streamChatEnabled);
            } else if (settingOption == GameSettings.Options.STREAM_CHAT_USER_FILTER) {
                return s1 + getTranslation(STREAM_CHAT_FILTER_MODES, streamChatUserFilter);
            } else if (settingOption == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR) {
                return s1 + getTranslation(STREAM_MIC_MODES, streamMicToggleBehavior);
            } else if (settingOption == GameSettings.Options.RENDER_CLOUDS) {
                return s1 + getTranslation(CLOUDS_TYPES, clouds);
            } else if (settingOption == GameSettings.Options.GRAPHICS) {
                if (fancyGraphics) {
                    return s1 + I18n.format("options.graphics.fancy");
                } else {
                    String s2 = "options.graphics.fast";
                    return s1 + I18n.format("options.graphics.fast");
                }
            } else {
                return s1;
            }
        }
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions() {
        FileInputStream fileinputstream = null;
        label2:
        {
            try {
                if (optionsFile.exists()) {
                    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(fileinputstream = new FileInputStream(optionsFile)));
                    String s = "";
                    mapSoundLevels.clear();

                    while ((s = bufferedreader.readLine()) != null) {
                        try {
                            String[] astring = s.split(":");

                            if (astring[0].equals("mouseSensitivity")) {
                                mouseSensitivity = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("fov")) {
                                fovSetting = parseFloat(astring[1]) * 40.0F + 70.0F;
                            }

                            if (astring[0].equals("fastplace")) {
                                fastplace = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("chunkupdateslimit")) {
                                chunkupdateslimit = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("autoclicker")) {
                                autoclicker = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("combatrange")) {
                                combatrange = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("velocityhori")) {
                                velocityhori = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("velocityvert")) {
                                velocityvert = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("nametag")) {
                                nametag = astring[1].equals("true");
                            }

                            if (astring[0].equals("viewclip")) {
                                viewclip = astring[1].equals("true");
                            }

                            if (astring[0].equals("autogg")) {
                                autogg = astring[1].equals("true");
                            }

                            if (astring[0].equals("autotool")) {
                                autotool = astring[1].equals("true");
                            }

                            if (astring[0].equals("moduleslist")) {
                                moduleslist = astring[1].equals("true");
                            }

                            if (astring[0].equals("rawmouseinput")) {
                                rawmouseinput = astring[1].equals("true");
                            }

                            if (astring[0].equals("blockinghit")) {
                                blockinghit = astring[1].equals("true");
                            }

                            if (astring[0].equals("noslow")) {
                                noslow = astring[1].equals("true");
                            }

                            if (astring[0].equals("eagle")) {
                                eagle = astring[1].equals("true");
                            }

                            if (astring[0].equals("gamma")) {
                                gammaSetting = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("saturation")) {
                                saturation = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("invertYMouse")) {
                                invertMouse = astring[1].equals("true");
                            }

                            if (astring[0].equals("renderDistance")) {
                                renderDistanceChunks = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("guiScale")) {
                                guiScale = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("particles")) {
                                particleSetting = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("bobView")) {
                                viewBobbing = astring[1].equals("true");
                            }

                            if (astring[0].equals("anaglyph3d")) {
                                anaglyph = astring[1].equals("true");
                            }

                            if (astring[0].equals("maxFps")) {
                                limitFramerate = Integer.parseInt(astring[1]);

                                if (enableVsync) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                if (limitFramerate <= 0) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }
                            }

                            if (astring[0].equals("fboEnable")) {
                                fboEnable = astring[1].equals("true");
                            }

                            if (astring[0].equals("difficulty")) {
                                difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("fancyGraphics")) {
                                fancyGraphics = astring[1].equals("true");
                                updateRenderClouds();
                            }

                            if (astring[0].equals("ao")) {
                                if (astring[1].equals("true")) {
                                    ambientOcclusion = 2;
                                } else if (astring[1].equals("false")) {
                                    ambientOcclusion = 0;
                                } else {
                                    ambientOcclusion = Integer.parseInt(astring[1]);
                                }
                            }

                            if (astring[0].equals("renderClouds")) {
                                switch (astring[1]) {
                                    case "true" -> clouds = 2;
                                    case "false" -> clouds = 0;
                                    case "fast" -> clouds = 1;
                                }
                            }

                            if (astring[0].equals("resourcePacks")) {
                                resourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (resourcePacks == null) {
                                    resourcePacks = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("incompatibleResourcePacks")) {
                                incompatibleResourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (incompatibleResourcePacks == null) {
                                    incompatibleResourcePacks = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("lastServer") && astring.length >= 2) {
                                lastServer = s.substring(s.indexOf(58) + 1);
                            }

                            if (astring[0].equals("lang") && astring.length >= 2) {
                                language = astring[1];
                            }

                            if (astring[0].equals("chatVisibility")) {
                                chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("chatColors")) {
                                chatColours = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinks")) {
                                chatLinks = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinksPrompt")) {
                                chatLinksPrompt = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatOpacity")) {
                                chatOpacity = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("snooperEnabled")) {
                                snooperEnabled = astring[1].equals("true");
                            }

                            if (astring[0].equals("fullscreen")) {
                                fullScreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("enableVsync")) {
                                enableVsync = astring[1].equals("true");

                                if (enableVsync) {
                                    limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                updateVSync();
                            }

                            if (astring[0].equals("useVbo")) {
                                useVbo = astring[1].equals("true");
                            }

                            if (astring[0].equals("hideServerAddress")) {
                                hideServerAddress = astring[1].equals("true");
                            }

                            if (astring[0].equals("advancedItemTooltips")) {
                                advancedItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("pauseOnLostFocus")) {
                                pauseOnLostFocus = astring[1].equals("true");
                            }

                            if (astring[0].equals("touchscreen")) {
                                touchscreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("overrideHeight")) {
                                overrideHeight = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("overrideWidth")) {
                                overrideWidth = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("heldItemTooltips")) {
                                heldItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatHeightFocused")) {
                                chatHeightFocused = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatHeightUnfocused")) {
                                chatHeightUnfocused = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatScale")) {
                                chatScale = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatWidth")) {
                                chatWidth = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("showInventoryAchievementHint")) {
                                showInventoryAchievementHint = astring[1].equals("true");
                            }

                            if (astring[0].equals("mipmapLevels")) {
                                mipmapLevels = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("streamBytesPerPixel")) {
                                streamBytesPerPixel = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("streamMicVolume")) {
                                streamMicVolume = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("streamSystemVolume")) {
                                streamGameVolume = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("streamKbps")) {
                                streamKbps = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("streamFps")) {
                                streamFps = parseFloat(astring[1]);
                            }

                            if (astring[0].equals("streamCompression")) {
                                streamCompression = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("streamSendMetadata")) {
                                streamSendMetadata = astring[1].equals("true");
                            }

                            if (astring[0].equals("streamPreferredServer") && astring.length >= 2) {
                                streamPreferredServer = s.substring(s.indexOf(58) + 1);
                            }

                            if (astring[0].equals("streamChatEnabled")) {
                                streamChatEnabled = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("streamChatUserFilter")) {
                                streamChatUserFilter = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("streamMicToggleBehavior")) {
                                streamMicToggleBehavior = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("forceUnicodeFont")) {
                                forceUnicodeFont = astring[1].equals("true");
                            }

                            if (astring[0].equals("allowBlockAlternatives")) {
                                allowBlockAlternatives = astring[1].equals("true");
                            }

                            if (astring[0].equals("reducedDebugInfo")) {
                                reducedDebugInfo = astring[1].equals("true");
                            }

                            if (astring[0].equals("useNativeTransport")) {
                                useNativeTransport = astring[1].equals("true");
                            }

                            if (astring[0].equals("entityShadows")) {
                                entityShadows = astring[1].equals("true");
                            }

                            if (astring[0].equals("realmsNotifications")) {
                                realmsNotifications = astring[1].equals("true");
                            }

                            for (KeyBinding keybinding : keyBindings) {
                                if (astring[0].equals("key_" + keybinding.getKeyDescription())) {
                                    keybinding.setKeyCode(Integer.parseInt(astring[1]));
                                }
                            }

                            for (SoundCategory soundcategory : SoundCategory.values()) {
                                if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName())) {
                                    mapSoundLevels.put(soundcategory, parseFloat(astring[1]));
                                }
                            }

                            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                                if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName())) {
                                    setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                                }
                            }
                        } catch (Exception exception) {
                            logger.warn("Skipping bad option: " + s);
                            exception.printStackTrace();
                        }
                    }

                    KeyBinding.resetKeyBindingArrayAndHash();
                    bufferedreader.close();
                    break label2;
                }
            } catch (Exception exception1) {
                logger.error("Failed to load options", exception1);
                break label2;
            } finally {
                IOUtils.closeQuietly(fileinputstream);
            }

            return;
        }
        loadOfOptions();
    }

    /**
     * Parses a string into a float.
     *
     * @param str The string to parse
     */
    private float parseFloat(String str) {
        return str.equals("true") ? 1.0F : (str.equals("false") ? 0.0F : Float.parseFloat(str));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions() {
        if (Reflector.FMLClientHandler.exists()) {
            Object object = Reflector.call(Reflector.FMLClientHandler_instance);

            if (object != null && Reflector.callBoolean(object, Reflector.FMLClientHandler_isLoading)) {
                return;
            }
        }

        try {
            PrintWriter printwriter = new PrintWriter(new FileWriter(optionsFile));
            printwriter.println("invertYMouse:" + invertMouse);
            printwriter.println("mouseSensitivity:" + mouseSensitivity);
            printwriter.println("fov:" + (fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + gammaSetting);
            printwriter.println("saturation:" + saturation);
            printwriter.println("renderDistance:" + renderDistanceChunks);
            printwriter.println("guiScale:" + guiScale);
            printwriter.println("particles:" + particleSetting);
            printwriter.println("bobView:" + viewBobbing);
            printwriter.println("anaglyph3d:" + anaglyph);
            printwriter.println("maxFps:" + limitFramerate);
            printwriter.println("fboEnable:" + fboEnable);
            printwriter.println("difficulty:" + difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + fancyGraphics);
            printwriter.println("ao:" + ambientOcclusion);
            switch (clouds) {
                case 0 -> printwriter.println("renderClouds:false");
                case 1 -> printwriter.println("renderClouds:fast");
                case 2 -> printwriter.println("renderClouds:true");
            }
            printwriter.println("resourcePacks:" + gson.toJson(resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + gson.toJson(incompatibleResourcePacks));
            printwriter.println("lastServer:" + lastServer);
            printwriter.println("lang:" + language);
            printwriter.println("chatVisibility:" + chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + chatColours);
            printwriter.println("chatLinks:" + chatLinks);
            printwriter.println("chatLinksPrompt:" + chatLinksPrompt);
            printwriter.println("chatOpacity:" + chatOpacity);
            printwriter.println("snooperEnabled:" + snooperEnabled);
            printwriter.println("fullscreen:" + fullScreen);
            printwriter.println("enableVsync:" + enableVsync);
            printwriter.println("useVbo:" + useVbo);
            printwriter.println("hideServerAddress:" + hideServerAddress);
            printwriter.println("advancedItemTooltips:" + advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + pauseOnLostFocus);
            printwriter.println("touchscreen:" + touchscreen);
            printwriter.println("overrideWidth:" + overrideWidth);
            printwriter.println("overrideHeight:" + overrideHeight);
            printwriter.println("heldItemTooltips:" + heldItemTooltips);
            printwriter.println("chatHeightFocused:" + chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + chatHeightUnfocused);
            printwriter.println("chatScale:" + chatScale);
            printwriter.println("chatWidth:" + chatWidth);
            printwriter.println("showInventoryAchievementHint:" + showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + mipmapLevels);
            printwriter.println("streamBytesPerPixel:" + streamBytesPerPixel);
            printwriter.println("streamMicVolume:" + streamMicVolume);
            printwriter.println("streamSystemVolume:" + streamGameVolume);
            printwriter.println("streamKbps:" + streamKbps);
            printwriter.println("streamFps:" + streamFps);
            printwriter.println("streamCompression:" + streamCompression);
            printwriter.println("nametag:" + nametag);
            printwriter.println("viewclip:" + viewclip);
            printwriter.println("autogg:" + autogg);
            printwriter.println("autotool:" + autotool);
            printwriter.println("moduleslist:" + moduleslist);
            printwriter.println("rawmouseinput:" + rawmouseinput);
            printwriter.println("blockinghit:" + blockinghit);
            printwriter.println("noslow:" + noslow);
            printwriter.println("eagle:" + eagle);
            printwriter.println("fastplace:" + fastplace);
            printwriter.println("chunkupdateslimit:" + chunkupdateslimit);
            printwriter.println("autoclicker:" + autoclicker);
            printwriter.println("velocityhori:" + velocityhori);
            printwriter.println("velocityvert:" + velocityvert);
            printwriter.println("combatrange:" + combatrange);
            printwriter.println("forceUnicodeFont:" + forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + reducedDebugInfo);
            printwriter.println("useNativeTransport:" + useNativeTransport);
            printwriter.println("entityShadows:" + entityShadows);
            printwriter.println("realmsNotifications:" + realmsNotifications);

            for (KeyBinding keybinding : keyBindings) {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values()) {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        } catch (Exception exception) {
            logger.error("Failed to save options", exception);
        }

        saveOfOptions();
        sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory sndCategory) {
        return mapSoundLevels.getOrDefault(sndCategory, 1.0F);
    }

    public void setSoundLevel(SoundCategory sndCategory, float soundLevel) {
        mc.getSoundHandler().setSoundLevel(sndCategory, soundLevel);
        mapSoundLevels.put(sndCategory, soundLevel);
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer() {
        if (mc.thePlayer != null) {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : setModelParts) {
                i |= enumplayermodelparts.getPartMask();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts() {
        return ImmutableSet.copyOf(setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable) {
        if (enable) {
            setModelParts.add(modelPart);
        } else {
            setModelParts.remove(modelPart);
        }

        sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts modelPart) {
        if (!getModelParts().contains(modelPart)) {
            setModelParts.add(modelPart);
        } else {
            setModelParts.remove(modelPart);
        }

        sendSettingsToServer();
    }

    /**
     * Return true if the client connect to a server using the native transport system
     */
    public boolean isUsingNativeTransport() {
        return useNativeTransport;
    }

    private void setOptionFloatValueOF(GameSettings.Options p_setOptionFloatValueOF_1_, float p_setOptionFloatValueOF_2_) {
        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.CLOUD_HEIGHT) {
            ofCloudsHeight = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AO_LEVEL) {
            ofAoLevel = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AA_LEVEL) {
            int i = (int) p_setOptionFloatValueOF_2_;

            if (i > 0 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            int[] aint = new int[]{0, 2, 4, 6, 8, 12, 16};
            ofAaLevel = 0;

            for (int k : aint) {
                if (i >= k) {
                    ofAaLevel = k;
                }
            }

            ofAaLevel = Config.limit(ofAaLevel, 0, 16);
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AF_LEVEL) {
            int k = (int) p_setOptionFloatValueOF_2_;

            if (k > 1 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                return;
            }

            for (ofAfLevel = 1; ofAfLevel * 2 <= k; ofAfLevel *= 2) {
            }

            ofAfLevel = Config.limit(ofAfLevel, 1, 16);
            mc.refreshResources();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.MIPMAP_TYPE) {
            int l = (int) p_setOptionFloatValueOF_2_;
            ofMipmapType = Config.limit(l, 0, 3);
            mc.refreshResources();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.FULLSCREEN_MODE) {
            int i1 = (int) p_setOptionFloatValueOF_2_ - 1;
            String[] astring = Config.getDisplayModeNames();

            if (i1 < 0 || i1 >= astring.length) {
                ofFullscreenMode = "Default";
                return;
            }

            ofFullscreenMode = astring[i1];
        }
    }

    private float getOptionFloatValueOF(GameSettings.Options p_getOptionFloatValueOF_1_) {
        if (p_getOptionFloatValueOF_1_ == GameSettings.Options.CLOUD_HEIGHT) {
            return ofCloudsHeight;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AO_LEVEL) {
            return ofAoLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AA_LEVEL) {
            return (float) ofAaLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.AF_LEVEL) {
            return (float) ofAfLevel;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.MIPMAP_TYPE) {
            return (float) ofMipmapType;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.FRAMERATE_LIMIT) {
            return (float) limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && enableVsync ? 0.0F : (float) limitFramerate;
        } else if (p_getOptionFloatValueOF_1_ == GameSettings.Options.FULLSCREEN_MODE) {
            if (ofFullscreenMode.equals("Default")) {
                return 0.0F;
            } else {
                List list = Arrays.asList(Config.getDisplayModeNames());
                int i = list.indexOf(ofFullscreenMode);
                return i < 0 ? 0.0F : (float) (i + 1);
            }
        } else {
            return Float.MAX_VALUE;
        }
    }

    private void setOptionValueOF(GameSettings.Options p_setOptionValueOF_1_, int p_setOptionValueOF_2_) {
        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_FANCY) {
            switch (ofFogType) {
                case 1 -> {
                    ofFogType = 2;
                    if (!Config.isFancyFogAvailable()) {
                        ofFogType = 3;
                    }
                }
                case 2 -> ofFogType = 3;
                default -> ofFogType = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_START) {
            ofFogStart += 0.2F;

            if (ofFogStart > 0.81F) {
                ofFogStart = 0.2F;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_FPS) {
            ofSmoothFps = !ofSmoothFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_WORLD) {
            ofSmoothWorld = !ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLOUDS) {
            ++ofClouds;

            if (ofClouds > 3) {
                ofClouds = 0;
            }

            updateRenderClouds();
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TREES) {
            ofTrees = nextValue(ofTrees, OF_TREES_VALUES);
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DROPPED_ITEMS) {
            ++ofDroppedItems;

            if (ofDroppedItems > 2) {
                ofDroppedItems = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN) {
            ++ofRain;

            if (ofRain > 3) {
                ofRain = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_WATER) {
            ++ofAnimatedWater;

            if (ofAnimatedWater == 1) {
                ++ofAnimatedWater;
            }

            if (ofAnimatedWater > 2) {
                ofAnimatedWater = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_LAVA) {
            ++ofAnimatedLava;

            if (ofAnimatedLava == 1) {
                ++ofAnimatedLava;
            }

            if (ofAnimatedLava > 2) {
                ofAnimatedLava = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FIRE) {
            ofAnimatedFire = !ofAnimatedFire;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_PORTAL) {
            ofAnimatedPortal = !ofAnimatedPortal;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_REDSTONE) {
            ofAnimatedRedstone = !ofAnimatedRedstone;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_EXPLOSION) {
            ofAnimatedExplosion = !ofAnimatedExplosion;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FLAME) {
            ofAnimatedFlame = !ofAnimatedFlame;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_SMOKE) {
            ofAnimatedSmoke = !ofAnimatedSmoke;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VOID_PARTICLES) {
            ofVoidParticles = !ofVoidParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WATER_PARTICLES) {
            ofWaterParticles = !ofWaterParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PORTAL_PARTICLES) {
            ofPortalParticles = !ofPortalParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.POTION_PARTICLES) {
            ofPotionParticles = !ofPotionParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FIREWORK_PARTICLES) {
            ofFireworkParticles = !ofFireworkParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DRIPPING_WATER_LAVA) {
            ofDrippingWaterLava = !ofDrippingWaterLava;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TERRAIN) {
            ofAnimatedTerrain = !ofAnimatedTerrain;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TEXTURES) {
            ofAnimatedTextures = !ofAnimatedTextures;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN_SPLASH) {
            ofRainSplash = !ofRainSplash;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAGOMETER) {
            ofLagometer = !ofLagometer;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_FPS) {
            ofShowFps = !ofShowFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.AUTOSAVE_TICKS) {
            int i = 900;
            ofAutoSaveTicks = Math.max(ofAutoSaveTicks / i * i, i);
            ofAutoSaveTicks *= 2;

            if (ofAutoSaveTicks > 32 * i) {
                ofAutoSaveTicks = i;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_GRASS) {
            ++ofBetterGrass;

            if (ofBetterGrass > 3) {
                ofBetterGrass = 1;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CONNECTED_TEXTURES) {
            ++ofConnectedTextures;

            if (ofConnectedTextures > 3) {
                ofConnectedTextures = 1;
            }

            if (ofConnectedTextures == 2) {
                mc.renderGlobal.loadRenderers();
            } else {
                mc.refreshResources();
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WEATHER) {
            ofWeather = !ofWeather;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SKY) {
            ofSky = !ofSky;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.STARS) {
            ofStars = !ofStars;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SUN_MOON) {
            ofSunMoon = !ofSunMoon;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VIGNETTE) {
            ++ofVignette;

            if (ofVignette > 2) {
                ofVignette = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES) {
            ++ofChunkUpdates;

            if (ofChunkUpdates > 5) {
                ofChunkUpdates = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
            ofChunkUpdatesDynamic = !ofChunkUpdatesDynamic;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TIME) {
            ++ofTime;

            if (ofTime > 2) {
                ofTime = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLEAR_WATER) {
            ofClearWater = !ofClearWater;
            updateWaterOpacity();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PROFILER) {
            ofProfiler = !ofProfiler;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_SNOW) {
            ofBetterSnow = !ofBetterSnow;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SWAMP_COLORS) {
            ofSwampColors = !ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RANDOM_ENTITIES) {
            ofRandomEntities = !ofRandomEntities;
            RandomEntities.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_BIOMES) {
            ofSmoothBiomes = !ofSmoothBiomes;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_FONTS) {
            ofCustomFonts = !ofCustomFonts;
            mc.fontRendererObj.onResourceManagerReload(Config.getResourceManager());
            mc.standardGalacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_COLORS) {
            ofCustomColors = !ofCustomColors;
            CustomColors.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_ITEMS) {
            ofCustomItems = !ofCustomItems;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_SKY) {
            ofCustomSky = !ofCustomSky;
            CustomSky.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_CAPES) {
            ofShowCapes = !ofShowCapes;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.NATURAL_TEXTURES) {
            ofNaturalTextures = !ofNaturalTextures;
            NaturalTextures.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.EMISSIVE_TEXTURES) {
            ofEmissiveTextures = !ofEmissiveTextures;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_MATH) {
            ofFastMath = !ofFastMath;
            MathHelper.fastMath = ofFastMath;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_RENDER) {
            if (!ofFastRender && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                return;
            }

            ofFastRender = !ofFastRender;

            if (ofFastRender) {
                mc.entityRenderer.stopUseShader();
            }

            Config.updateFramebufferSize();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TRANSLUCENT_BLOCKS) {
            switch (ofTranslucentBlocks) {
                case 0 -> ofTranslucentBlocks = 1;
                case 1 -> ofTranslucentBlocks = 2;
                case 2 -> ofTranslucentBlocks = 0;
                default -> ofTranslucentBlocks = 0;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAZY_CHUNK_LOADING) {
            ofLazyChunkLoading = !ofLazyChunkLoading;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RENDER_REGIONS) {
            ofRenderRegions = !ofRenderRegions;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMART_ANIMATIONS) {
            ofSmartAnimations = !ofSmartAnimations;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_FOV) {
            ofDynamicFov = !ofDynamicFov;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ALTERNATE_BLOCKS) {
            ofAlternateBlocks = !ofAlternateBlocks;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_LIGHTS) {
            ofDynamicLights = nextValue(ofDynamicLights, OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(mc.renderGlobal);
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SCREENSHOT_SIZE) {
            ++ofScreenshotSize;

            if (ofScreenshotSize > 4) {
                ofScreenshotSize = 1;
            }

            if (!OpenGlHelper.isFramebufferEnabled()) {
                ofScreenshotSize = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_ENTITY_MODELS) {
            ofCustomEntityModels = !ofCustomEntityModels;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_GUIS) {
            ofCustomGuis = !ofCustomGuis;
            CustomGuis.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_GL_ERRORS) {
            ofShowGlErrors = !ofShowGlErrors;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
            heldItemTooltips = !heldItemTooltips;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ADVANCED_TOOLTIPS) {
            advancedItemTooltips = !advancedItemTooltips;
        }
    }

    private String getKeyBindingOF(GameSettings.Options p_getKeyBindingOF_1_) {
        String s = I18n.format(p_getKeyBindingOF_1_.getEnumString()) + ": ";

        switch (p_getKeyBindingOF_1_) {
            case RENDER_DISTANCE:
                int i1 = (int) getOptionFloatValue(Options.RENDER_DISTANCE);
                String s1 = I18n.format("options.renderDistance.tiny");
                int i = 2;

                if (i1 >= 4) {
                    s1 = I18n.format("options.renderDistance.short");
                    i = 4;
                }

                if (i1 >= 8) {
                    s1 = I18n.format("options.renderDistance.normal");
                    i = 8;
                }

                if (i1 >= 16) {
                    s1 = I18n.format("options.renderDistance.far");
                    i = 16;
                }

                if (i1 >= 32) {
                    s1 = Lang.get("of.options.renderDistance.extreme");
                    i = 32;
                }

                if (i1 >= 48) {
                    s1 = Lang.get("of.options.renderDistance.insane");
                    i = 48;
                }

                if (i1 >= 64) {
                    s1 = Lang.get("of.options.renderDistance.ludicrous");
                    i = 64;
                }

                int j = renderDistanceChunks - i;
                String s2 = s1;

                if (j > 0) {
                    s2 = s1 + "+";
                }

                return s + i1 + " " + s2 + "";
            case FOG_FANCY:
                return switch (ofFogType) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    case 3 -> s + Lang.getOff();
                    default -> s + Lang.getOff();
                };
            case FOG_START:
                return s + ofFogStart;
            case MIPMAP_TYPE:
                return switch (ofMipmapType) {
                    case 0 -> s + Lang.get("of.options.mipmap.nearest");
                    case 1 -> s + Lang.get("of.options.mipmap.linear");
                    case 2 -> s + Lang.get("of.options.mipmap.bilinear");
                    case 3 -> s + Lang.get("of.options.mipmap.trilinear");
                    default -> s + "of.options.mipmap.nearest";
                };
            case SMOOTH_FPS:
                return ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
            case SMOOTH_WORLD:
                return ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
            case CLOUDS:
                return switch (ofClouds) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    case 3 -> s + Lang.getOff();
                    default -> s + Lang.getDefault();
                };
            case TREES:
                return switch (ofTrees) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    case 3 -> s + Lang.getDefault();
                    case 4 -> s + Lang.get("of.general.smart");
                    default -> s + Lang.getDefault();
                };
            case DROPPED_ITEMS:
                return switch (ofDroppedItems) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    default -> s + Lang.getDefault();
                };
            case RAIN:
                return switch (ofRain) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    case 3 -> s + Lang.getOff();
                    default -> s + Lang.getDefault();
                };
            case ANIMATED_WATER:
                return switch (ofAnimatedWater) {
                    case 1 -> s + Lang.get("of.options.animation.dynamic");
                    case 2 -> s + Lang.getOff();
                    default -> s + Lang.getOn();
                };
            case ANIMATED_LAVA:
                return switch (ofAnimatedLava) {
                    case 1 -> s + Lang.get("of.options.animation.dynamic");
                    case 2 -> s + Lang.getOff();
                    default -> s + Lang.getOn();
                };
            case ANIMATED_FIRE:
                return ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_PORTAL:
                return ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_REDSTONE:
                return ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_EXPLOSION:
                return ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_FLAME:
                return ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_SMOKE:
                return ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
            case VOID_PARTICLES:
                return ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
            case WATER_PARTICLES:
                return ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
            case PORTAL_PARTICLES:
                return ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
            case POTION_PARTICLES:
                return ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
            case FIREWORK_PARTICLES:
                return ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
            case DRIPPING_WATER_LAVA:
                return ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_TERRAIN:
                return ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
            case ANIMATED_TEXTURES:
                return ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
            case RAIN_SPLASH:
                return ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
            case LAGOMETER:
                return ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
            case SHOW_FPS:
                return ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
            case AUTOSAVE_TICKS:
                int l = 900;
                return ofAutoSaveTicks <= l ? s + Lang.get("of.options.save.45s") : (ofAutoSaveTicks <= 2 * l ? s + Lang.get("of.options.save.90s") : (ofAutoSaveTicks <= 4 * l ? s + Lang.get("of.options.save.3min") : (ofAutoSaveTicks <= 8 * l ? s + Lang.get("of.options.save.6min") : (ofAutoSaveTicks <= 16 * l ? s + Lang.get("of.options.save.12min") : s + Lang.get("of.options.save.24min")))));
            case BETTER_GRASS:
                return switch (ofBetterGrass) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    default -> s + Lang.getOff();
                };
            case CONNECTED_TEXTURES:
                return switch (ofConnectedTextures) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    default -> s + Lang.getOff();
                };
            case WEATHER:
                return ofWeather ? s + Lang.getOn() : s + Lang.getOff();
            case SKY:
                return ofSky ? s + Lang.getOn() : s + Lang.getOff();
            case STARS:
                return ofStars ? s + Lang.getOn() : s + Lang.getOff();
            case SUN_MOON:
                return ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
            case VIGNETTE:
                return switch (ofVignette) {
                    case 1 -> s + Lang.getFast();
                    case 2 -> s + Lang.getFancy();
                    default -> s + Lang.getDefault();
                };
            case CHUNK_UPDATES:
                return s + ofChunkUpdates;
            case CHUNK_UPDATES_DYNAMIC:
                return ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
            case TIME:
                return ofTime == 1 ? s + Lang.get("of.options.time.dayOnly") : (ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault());
            case CLEAR_WATER:
                return ofClearWater ? s + Lang.getOn() : s + Lang.getOff();
            case AA_LEVEL:
                String s3 = "";

                if (ofAaLevel != Config.getAntialiasingLevel()) {
                    s3 = " (" + Lang.get("of.general.restart") + ")";
                }

                return ofAaLevel == 0 ? s + Lang.getOff() + s3 : s + ofAaLevel + s3;
            case AF_LEVEL:
                return ofAfLevel == 1 ? s + Lang.getOff() : s + ofAfLevel;
            case PROFILER:
                return ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
            case BETTER_SNOW:
                return ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
            case SWAMP_COLORS:
                return ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
            case RANDOM_ENTITIES:
                return ofRandomEntities ? s + Lang.getOn() : s + Lang.getOff();
            case SMOOTH_BIOMES:
                return ofSmoothBiomes ? s + Lang.getOn() : s + Lang.getOff();
            case CUSTOM_FONTS:
                return ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
            case CUSTOM_COLORS:
                return ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
            case CUSTOM_SKY:
                return ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
            case SHOW_CAPES:
                return ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
            case CUSTOM_ITEMS:
                return ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
            case NATURAL_TEXTURES:
                return ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
            case EMISSIVE_TEXTURES:
                return ofEmissiveTextures ? s + Lang.getOn() : s + Lang.getOff();
            case FAST_MATH:
                return ofFastMath ? s + Lang.getOn() : s + Lang.getOff();
            case FAST_RENDER:
                return ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
            case TRANSLUCENT_BLOCKS:
                return ofTranslucentBlocks == 1 ? s + Lang.getFast() : (ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault());
            case LAZY_CHUNK_LOADING:
                return ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
            case RENDER_REGIONS:
                return ofRenderRegions ? s + Lang.getOn() : s + Lang.getOff();
            case SMART_ANIMATIONS:
                return ofSmartAnimations ? s + Lang.getOn() : s + Lang.getOff();
            case DYNAMIC_FOV:
                return ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
            case ALTERNATE_BLOCKS:
                return ofAlternateBlocks ? s + Lang.getOn() : s + Lang.getOff();
            case DYNAMIC_LIGHTS:
                int k = indexOf(ofDynamicLights, OF_DYNAMIC_LIGHTS);
                return s + getTranslation(KEYS_DYNAMIC_LIGHTS, k);
            case SCREENSHOT_SIZE:
                return ofScreenshotSize <= 1 ? s + Lang.getDefault() : s + ofScreenshotSize + "x";
            case CUSTOM_ENTITY_MODELS:
                return ofCustomEntityModels ? s + Lang.getOn() : s + Lang.getOff();
            case CUSTOM_GUIS:
                return ofCustomGuis ? s + Lang.getOn() : s + Lang.getOff();
            case SHOW_GL_ERRORS:
                return ofShowGlErrors ? s + Lang.getOn() : s + Lang.getOff();
            case FULLSCREEN_MODE:
                return ofFullscreenMode.equals("Default") ? s + Lang.getDefault() : s + ofFullscreenMode;
            case HELD_ITEM_TOOLTIPS:
                return heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
            case ADVANCED_TOOLTIPS:
                return advancedItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
            case FRAMERATE_LIMIT:
                float f = getOptionFloatValue(Options.FRAMERATE_LIMIT);
                return f == 0.0F ? s + Lang.get("of.options.framerateLimit.vsync") : (f == p_getKeyBindingOF_1_.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int) f + " fps");
            default:
                return null;
        }
    }

    public void loadOfOptions() {
        try {
            File file1 = optionsFileOF;

            if (!file1.exists()) {
                file1 = optionsFile;
            }

            if (!file1.exists()) {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8));
            String s = "";

            while ((s = bufferedreader.readLine()) != null) {
                try {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2) {
                        renderDistanceChunks = Integer.valueOf(astring[1]);
                        renderDistanceChunks = Config.limit(renderDistanceChunks, 2, 1024);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2) {
                        ofFogType = Integer.valueOf(astring[1]);
                        ofFogType = Config.limit(ofFogType, 1, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2) {
                        ofFogStart = Float.valueOf(astring[1]);

                        if (ofFogStart < 0.2F) {
                            ofFogStart = 0.2F;
                        }

                        if (ofFogStart > 0.81F) {
                            ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2) {
                        ofMipmapType = Integer.valueOf(astring[1]);
                        ofMipmapType = Config.limit(ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2) {
                        ofOcclusionFancy = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2) {
                        ofSmoothFps = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2) {
                        ofSmoothWorld = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2) {
                        ofAoLevel = Float.valueOf(astring[1]);
                        ofAoLevel = Config.limit(ofAoLevel, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2) {
                        ofClouds = Integer.valueOf(astring[1]);
                        ofClouds = Config.limit(ofClouds, 0, 3);
                        updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2) {
                        ofCloudsHeight = Float.valueOf(astring[1]);
                        ofCloudsHeight = Config.limit(ofCloudsHeight, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2) {
                        ofTrees = Integer.valueOf(astring[1]);
                        ofTrees = limit(ofTrees, OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2) {
                        ofDroppedItems = Integer.valueOf(astring[1]);
                        ofDroppedItems = Config.limit(ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2) {
                        ofRain = Integer.valueOf(astring[1]);
                        ofRain = Config.limit(ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2) {
                        ofAnimatedWater = Integer.valueOf(astring[1]);
                        ofAnimatedWater = Config.limit(ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2) {
                        ofAnimatedLava = Integer.valueOf(astring[1]);
                        ofAnimatedLava = Config.limit(ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2) {
                        ofAnimatedFire = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2) {
                        ofAnimatedPortal = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2) {
                        ofAnimatedRedstone = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2) {
                        ofAnimatedExplosion = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2) {
                        ofAnimatedFlame = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2) {
                        ofAnimatedSmoke = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2) {
                        ofVoidParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2) {
                        ofWaterParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2) {
                        ofPortalParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2) {
                        ofPotionParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2) {
                        ofFireworkParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2) {
                        ofDrippingWaterLava = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2) {
                        ofAnimatedTerrain = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2) {
                        ofAnimatedTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2) {
                        ofRainSplash = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2) {
                        ofLagometer = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2) {
                        ofShowFps = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2) {
                        ofAutoSaveTicks = Integer.valueOf(astring[1]).intValue();
                        ofAutoSaveTicks = Config.limit(ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2) {
                        ofBetterGrass = Integer.valueOf(astring[1]).intValue();
                        ofBetterGrass = Config.limit(ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2) {
                        ofConnectedTextures = Integer.valueOf(astring[1]).intValue();
                        ofConnectedTextures = Config.limit(ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2) {
                        ofWeather = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2) {
                        ofSky = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2) {
                        ofStars = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2) {
                        ofSunMoon = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2) {
                        ofVignette = Integer.valueOf(astring[1]);
                        ofVignette = Config.limit(ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2) {
                        ofChunkUpdates = Integer.valueOf(astring[1]);
                        ofChunkUpdates = Config.limit(ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2) {
                        ofChunkUpdatesDynamic = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2) {
                        ofTime = Integer.valueOf(astring[1]);
                        ofTime = Config.limit(ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofClearWater") && astring.length >= 2) {
                        ofClearWater = Boolean.valueOf(astring[1]);
                        updateWaterOpacity();
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2) {
                        ofAaLevel = Integer.valueOf(astring[1]);
                        ofAaLevel = Config.limit(ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2) {
                        ofAfLevel = Integer.valueOf(astring[1]);
                        ofAfLevel = Config.limit(ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2) {
                        ofProfiler = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2) {
                        ofBetterSnow = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2) {
                        ofSwampColors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofRandomEntities") && astring.length >= 2) {
                        ofRandomEntities = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothBiomes") && astring.length >= 2) {
                        ofSmoothBiomes = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2) {
                        ofCustomFonts = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2) {
                        ofCustomColors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2) {
                        ofCustomItems = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2) {
                        ofCustomSky = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2) {
                        ofShowCapes = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2) {
                        ofNaturalTextures = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofEmissiveTextures") && astring.length >= 2) {
                        ofEmissiveTextures = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2) {
                        ofLazyChunkLoading = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofRenderRegions") && astring.length >= 2) {
                        ofRenderRegions = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmartAnimations") && astring.length >= 2) {
                        ofSmartAnimations = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2) {
                        ofDynamicFov = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAlternateBlocks") && astring.length >= 2) {
                        ofAlternateBlocks = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2) {
                        ofDynamicLights = Integer.valueOf(astring[1]);
                        ofDynamicLights = limit(ofDynamicLights, OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofScreenshotSize") && astring.length >= 2) {
                        ofScreenshotSize = Integer.valueOf(astring[1]);
                        ofScreenshotSize = Config.limit(ofScreenshotSize, 1, 4);
                    }

                    if (astring[0].equals("ofCustomEntityModels") && astring.length >= 2) {
                        ofCustomEntityModels = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomGuis") && astring.length >= 2) {
                        ofCustomGuis = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofShowGlErrors") && astring.length >= 2) {
                        ofShowGlErrors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofFullscreenMode") && astring.length >= 2) {
                        ofFullscreenMode = astring[1];
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2) {
                        ofFastMath = Boolean.valueOf(astring[1]);
                        MathHelper.fastMath = ofFastMath;
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2) {
                        ofFastRender = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2) {
                        ofTranslucentBlocks = Integer.valueOf(astring[1]);
                        ofTranslucentBlocks = Config.limit(ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("key_" + ofKeyBindZoom.getKeyDescription())) {
                        ofKeyBindZoom.setKeyCode(Integer.parseInt(astring[1]));
                    }
                } catch (Exception exception) {
                    Config.dbg("Skipping bad option: " + s);
                    exception.printStackTrace();
                }
            }

            KeyUtils.fixKeyConflicts(keyBindings, new KeyBinding[]{ofKeyBindZoom});
            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        } catch (Exception exception1) {
            Config.warn("Failed to load options");
            exception1.printStackTrace();
        }
    }

    public void saveOfOptions() {
        try {
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFileOF), StandardCharsets.UTF_8));
            printwriter.println("ofFogType:" + ofFogType);
            printwriter.println("ofFogStart:" + ofFogStart);
            printwriter.println("ofMipmapType:" + ofMipmapType);
            printwriter.println("ofOcclusionFancy:" + ofOcclusionFancy);
            printwriter.println("ofSmoothFps:" + ofSmoothFps);
            printwriter.println("ofSmoothWorld:" + ofSmoothWorld);
            printwriter.println("ofAoLevel:" + ofAoLevel);
            printwriter.println("ofClouds:" + ofClouds);
            printwriter.println("ofCloudsHeight:" + ofCloudsHeight);
            printwriter.println("ofTrees:" + ofTrees);
            printwriter.println("ofDroppedItems:" + ofDroppedItems);
            printwriter.println("ofRain:" + ofRain);
            printwriter.println("ofAnimatedWater:" + ofAnimatedWater);
            printwriter.println("ofAnimatedLava:" + ofAnimatedLava);
            printwriter.println("ofAnimatedFire:" + ofAnimatedFire);
            printwriter.println("ofAnimatedPortal:" + ofAnimatedPortal);
            printwriter.println("ofAnimatedRedstone:" + ofAnimatedRedstone);
            printwriter.println("ofAnimatedExplosion:" + ofAnimatedExplosion);
            printwriter.println("ofAnimatedFlame:" + ofAnimatedFlame);
            printwriter.println("ofAnimatedSmoke:" + ofAnimatedSmoke);
            printwriter.println("ofVoidParticles:" + ofVoidParticles);
            printwriter.println("ofWaterParticles:" + ofWaterParticles);
            printwriter.println("ofPortalParticles:" + ofPortalParticles);
            printwriter.println("ofPotionParticles:" + ofPotionParticles);
            printwriter.println("ofFireworkParticles:" + ofFireworkParticles);
            printwriter.println("ofDrippingWaterLava:" + ofDrippingWaterLava);
            printwriter.println("ofAnimatedTerrain:" + ofAnimatedTerrain);
            printwriter.println("ofAnimatedTextures:" + ofAnimatedTextures);
            printwriter.println("ofRainSplash:" + ofRainSplash);
            printwriter.println("ofLagometer:" + ofLagometer);
            printwriter.println("ofShowFps:" + ofShowFps);
            printwriter.println("ofAutoSaveTicks:" + ofAutoSaveTicks);
            printwriter.println("ofBetterGrass:" + ofBetterGrass);
            printwriter.println("ofConnectedTextures:" + ofConnectedTextures);
            printwriter.println("ofWeather:" + ofWeather);
            printwriter.println("ofSky:" + ofSky);
            printwriter.println("ofStars:" + ofStars);
            printwriter.println("ofSunMoon:" + ofSunMoon);
            printwriter.println("ofVignette:" + ofVignette);
            printwriter.println("ofChunkUpdates:" + ofChunkUpdates);
            printwriter.println("ofChunkUpdatesDynamic:" + ofChunkUpdatesDynamic);
            printwriter.println("ofTime:" + ofTime);
            printwriter.println("ofClearWater:" + ofClearWater);
            printwriter.println("ofAaLevel:" + ofAaLevel);
            printwriter.println("ofAfLevel:" + ofAfLevel);
            printwriter.println("ofProfiler:" + ofProfiler);
            printwriter.println("ofBetterSnow:" + ofBetterSnow);
            printwriter.println("ofSwampColors:" + ofSwampColors);
            printwriter.println("ofRandomEntities:" + ofRandomEntities);
            printwriter.println("ofSmoothBiomes:" + ofSmoothBiomes);
            printwriter.println("ofCustomFonts:" + ofCustomFonts);
            printwriter.println("ofCustomColors:" + ofCustomColors);
            printwriter.println("ofCustomItems:" + ofCustomItems);
            printwriter.println("ofCustomSky:" + ofCustomSky);
            printwriter.println("ofShowCapes:" + ofShowCapes);
            printwriter.println("ofNaturalTextures:" + ofNaturalTextures);
            printwriter.println("ofEmissiveTextures:" + ofEmissiveTextures);
            printwriter.println("ofLazyChunkLoading:" + ofLazyChunkLoading);
            printwriter.println("ofRenderRegions:" + ofRenderRegions);
            printwriter.println("ofSmartAnimations:" + ofSmartAnimations);
            printwriter.println("ofDynamicFov:" + ofDynamicFov);
            printwriter.println("ofAlternateBlocks:" + ofAlternateBlocks);
            printwriter.println("ofDynamicLights:" + ofDynamicLights);
            printwriter.println("ofScreenshotSize:" + ofScreenshotSize);
            printwriter.println("ofCustomEntityModels:" + ofCustomEntityModels);
            printwriter.println("ofCustomGuis:" + ofCustomGuis);
            printwriter.println("ofShowGlErrors:" + ofShowGlErrors);
            printwriter.println("ofFullscreenMode:" + ofFullscreenMode);
            printwriter.println("ofFastMath:" + ofFastMath);
            printwriter.println("ofFastRender:" + ofFastRender);
            printwriter.println("ofTranslucentBlocks:" + ofTranslucentBlocks);
            printwriter.println("key_" + ofKeyBindZoom.getKeyDescription() + ":" + ofKeyBindZoom.getKeyCode());
            printwriter.close();
        } catch (Exception exception) {
            Config.warn("Failed to save options");
            exception.printStackTrace();
        }
    }

    private void updateRenderClouds() {
        switch (ofClouds) {
            case 1:
                clouds = 1;
                break;

            case 2:
                clouds = 2;
                break;

            case 3:
                clouds = 0;
                break;

            default:
                if (fancyGraphics) {
                    clouds = 2;
                } else {
                    clouds = 1;
                }
        }
    }

    public void resetSettings() {
        renderDistanceChunks = 8;
        viewBobbing = true;
        anaglyph = false;
        limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        enableVsync = false;
        updateVSync();
        mipmapLevels = 4;
        fancyGraphics = true;
        ambientOcclusion = 2;
        clouds = 2;
        nametag = false;
        fovSetting = 70.0F;
        velocityvert = 100.0F;
        velocityhori = 100.0F;
        combatrange = 3.0F;
        fastplace = 5;
        rawmouseinput = false;
        chunkupdateslimit = 250;
        autoclicker = 0;
        gammaSetting = 0.0F;
        guiScale = 0;
        particleSetting = 0;
        heldItemTooltips = true;
        useVbo = false;
        forceUnicodeFont = false;
        ofFogType = 1;
        ofFogStart = 0.8F;
        ofMipmapType = 0;
        ofOcclusionFancy = false;
        ofSmartAnimations = false;
        ofSmoothFps = false;
        Config.updateAvailableProcessors();
        ofSmoothWorld = Config.isSingleProcessor();
        ofLazyChunkLoading = false;
        ofRenderRegions = false;
        ofFastMath = false;
        ofFastRender = false;
        ofTranslucentBlocks = 0;
        ofDynamicFov = true;
        ofAlternateBlocks = true;
        ofDynamicLights = 3;
        ofScreenshotSize = 1;
        ofCustomEntityModels = true;
        ofCustomGuis = true;
        ofShowGlErrors = true;
        ofAoLevel = 1.0F;
        ofAaLevel = 0;
        ofAfLevel = 1;
        ofClouds = 0;
        ofCloudsHeight = 0.0F;
        ofTrees = 0;
        ofRain = 0;
        ofBetterGrass = 3;
        ofAutoSaveTicks = 4000;
        ofLagometer = false;
        ofShowFps = false;
        ofProfiler = false;
        ofWeather = true;
        ofSky = true;
        ofStars = true;
        ofSunMoon = true;
        ofVignette = 0;
        ofChunkUpdates = 1;
        ofChunkUpdatesDynamic = false;
        ofTime = 0;
        ofClearWater = false;
        ofBetterSnow = false;
        ofFullscreenMode = "Default";
        ofSwampColors = true;
        ofRandomEntities = true;
        ofSmoothBiomes = true;
        ofCustomFonts = true;
        ofCustomColors = true;
        ofCustomItems = true;
        ofCustomSky = true;
        ofShowCapes = true;
        ofConnectedTextures = 2;
        ofNaturalTextures = false;
        ofEmissiveTextures = true;
        ofAnimatedWater = 0;
        ofAnimatedLava = 0;
        ofAnimatedFire = true;
        ofAnimatedPortal = true;
        ofAnimatedRedstone = true;
        ofAnimatedExplosion = true;
        ofAnimatedFlame = true;
        ofAnimatedSmoke = true;
        ofVoidParticles = true;
        ofWaterParticles = true;
        ofRainSplash = true;
        ofPortalParticles = true;
        ofPotionParticles = true;
        ofFireworkParticles = true;
        ofDrippingWaterLava = true;
        ofAnimatedTerrain = true;
        ofAnimatedTextures = true;
        Shaders.setShaderPack("OFF");
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        updateWaterOpacity();
        mc.refreshResources();
        saveOptions();
    }

    public void updateVSync() {
        Display.setVSyncEnabled(enableVsync);
    }

    private void updateWaterOpacity() {
        if (Config.isIntegratedServerRunning()) {
            Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, mc.theWorld);
    }

    public void setAllAnimations(boolean p_setAllAnimations_1_) {
        int i = p_setAllAnimations_1_ ? 0 : 2;
        ofAnimatedWater = i;
        ofAnimatedLava = i;
        ofAnimatedFire = p_setAllAnimations_1_;
        ofAnimatedPortal = p_setAllAnimations_1_;
        ofAnimatedRedstone = p_setAllAnimations_1_;
        ofAnimatedExplosion = p_setAllAnimations_1_;
        ofAnimatedFlame = p_setAllAnimations_1_;
        ofAnimatedSmoke = p_setAllAnimations_1_;
        ofVoidParticles = p_setAllAnimations_1_;
        ofWaterParticles = p_setAllAnimations_1_;
        ofRainSplash = p_setAllAnimations_1_;
        ofPortalParticles = p_setAllAnimations_1_;
        ofPotionParticles = p_setAllAnimations_1_;
        ofFireworkParticles = p_setAllAnimations_1_;
        particleSetting = p_setAllAnimations_1_ ? 0 : 2;
        ofDrippingWaterLava = p_setAllAnimations_1_;
        ofAnimatedTerrain = p_setAllAnimations_1_;
        ofAnimatedTextures = p_setAllAnimations_1_;
    }

    public enum Options {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        STREAM_BYTES_PER_PIXEL("options.stream.bytesPerPixel", true, false),
        STREAM_VOLUME_MIC("options.stream.micVolumne", true, false),
        STREAM_VOLUME_SYSTEM("options.stream.systemVolume", true, false),
        STREAM_KBPS("options.stream.kbps", true, false),
        STREAM_FPS("options.stream.fps", true, false),
        STREAM_COMPRESSION("options.stream.compression", false, false),
        STREAM_SEND_METADATA("options.stream.sendMetadata", false, true),
        STREAM_CHAT_ENABLED("options.stream.chat.enabled", false, false),
        STREAM_CHAT_USER_FILTER("options.stream.chat.userFilter", false, false),
        STREAM_MIC_TOGGLE_BEHAVIOR("options.stream.micToggleBehavior", false, false),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true),
        REALMS_NOTIFICATIONS("options.realmsNotifications", false, true),
        FOG_FANCY("of.options.FOG_FANCY", false, false),
        FOG_START("of.options.FOG_START", false, false),
        MIPMAP_TYPE("of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
        SMOOTH_FPS("of.options.SMOOTH_FPS", false, false),
        CLOUDS("of.options.CLOUDS", false, false),
        CLOUD_HEIGHT("of.options.CLOUD_HEIGHT", true, false),
        TREES("of.options.TREES", false, false),
        RAIN("of.options.RAIN", false, false),
        ANIMATED_WATER("of.options.ANIMATED_WATER", false, false),
        ANIMATED_LAVA("of.options.ANIMATED_LAVA", false, false),
        ANIMATED_FIRE("of.options.ANIMATED_FIRE", false, false),
        ANIMATED_PORTAL("of.options.ANIMATED_PORTAL", false, false),
        AO_LEVEL("of.options.AO_LEVEL", true, false),
        LAGOMETER("of.options.LAGOMETER", false, false),
        SHOW_FPS("of.options.SHOW_FPS", false, false),
        AUTOSAVE_TICKS("of.options.AUTOSAVE_TICKS", false, false),
        BETTER_GRASS("of.options.BETTER_GRASS", false, false),
        ANIMATED_REDSTONE("of.options.ANIMATED_REDSTONE", false, false),
        ANIMATED_EXPLOSION("of.options.ANIMATED_EXPLOSION", false, false),
        ANIMATED_FLAME("of.options.ANIMATED_FLAME", false, false),
        ANIMATED_SMOKE("of.options.ANIMATED_SMOKE", false, false),
        WEATHER("of.options.WEATHER", false, false),
        SKY("of.options.SKY", false, false),
        STARS("of.options.STARS", false, false),
        SUN_MOON("of.options.SUN_MOON", false, false),
        VIGNETTE("of.options.VIGNETTE", false, false),
        CHUNK_UPDATES("of.options.CHUNK_UPDATES", false, false),
        CHUNK_UPDATES_DYNAMIC("of.options.CHUNK_UPDATES_DYNAMIC", false, false),
        TIME("of.options.TIME", false, false),
        CLEAR_WATER("of.options.CLEAR_WATER", false, false),
        SMOOTH_WORLD("of.options.SMOOTH_WORLD", false, false),
        VOID_PARTICLES("of.options.VOID_PARTICLES", false, false),
        WATER_PARTICLES("of.options.WATER_PARTICLES", false, false),
        RAIN_SPLASH("of.options.RAIN_SPLASH", false, false),
        PORTAL_PARTICLES("of.options.PORTAL_PARTICLES", false, false),
        POTION_PARTICLES("of.options.POTION_PARTICLES", false, false),
        FIREWORK_PARTICLES("of.options.FIREWORK_PARTICLES", false, false),
        PROFILER("of.options.PROFILER", false, false),
        DRIPPING_WATER_LAVA("of.options.DRIPPING_WATER_LAVA", false, false),
        BETTER_SNOW("of.options.BETTER_SNOW", false, false),
        FULLSCREEN_MODE("of.options.FULLSCREEN_MODE", true, false, 0.0F, (float) Config.getDisplayModes().length, 1.0F),
        ANIMATED_TERRAIN("of.options.ANIMATED_TERRAIN", false, false),
        SWAMP_COLORS("of.options.SWAMP_COLORS", false, false),
        RANDOM_ENTITIES("of.options.RANDOM_ENTITIES", false, false),
        SMOOTH_BIOMES("of.options.SMOOTH_BIOMES", false, false),
        CUSTOM_FONTS("of.options.CUSTOM_FONTS", false, false),
        CUSTOM_COLORS("of.options.CUSTOM_COLORS", false, false),
        SHOW_CAPES("of.options.SHOW_CAPES", false, false),
        CONNECTED_TEXTURES("of.options.CONNECTED_TEXTURES", false, false),
        CUSTOM_ITEMS("of.options.CUSTOM_ITEMS", false, false),
        AA_LEVEL("of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
        AF_LEVEL("of.options.AF_LEVEL", true, false, 1.0F, 16.0F, 1.0F),
        ANIMATED_TEXTURES("of.options.ANIMATED_TEXTURES", false, false),
        NATURAL_TEXTURES("of.options.NATURAL_TEXTURES", false, false),
        EMISSIVE_TEXTURES("of.options.EMISSIVE_TEXTURES", false, false),
        HELD_ITEM_TOOLTIPS("of.options.HELD_ITEM_TOOLTIPS", false, false),
        DROPPED_ITEMS("of.options.DROPPED_ITEMS", false, false),
        LAZY_CHUNK_LOADING("of.options.LAZY_CHUNK_LOADING", false, false),
        CUSTOM_SKY("of.options.CUSTOM_SKY", false, false),
        FAST_MATH("of.options.FAST_MATH", false, false),
        FAST_RENDER("of.options.FAST_RENDER", false, false),
        TRANSLUCENT_BLOCKS("of.options.TRANSLUCENT_BLOCKS", false, false),
        DYNAMIC_FOV("of.options.DYNAMIC_FOV", false, false),
        DYNAMIC_LIGHTS("of.options.DYNAMIC_LIGHTS", false, false),
        ALTERNATE_BLOCKS("of.options.ALTERNATE_BLOCKS", false, false),
        CUSTOM_ENTITY_MODELS("of.options.CUSTOM_ENTITY_MODELS", false, false),
        ADVANCED_TOOLTIPS("of.options.ADVANCED_TOOLTIPS", false, false),
        SCREENSHOT_SIZE("of.options.SCREENSHOT_SIZE", false, false),
        CUSTOM_GUIS("of.options.CUSTOM_GUIS", false, false),
        RENDER_REGIONS("of.options.RENDER_REGIONS", false, false),
        SHOW_GL_ERRORS("of.options.SHOW_GL_ERRORS", false, false),
        SMART_ANIMATIONS("of.options.SMART_ANIMATIONS", false, false),
        CLIENT_COMBAT_VELOCITY_HORI("Velocity-Hori", true, false, 0.0F, 100.0F, 1.0F),
        CLIENT_COMBAT_VELOCITY_VERT("Velocity-Vert", true, false, 0.0F, 100.0F, 1.0F),
        CLIENT_COMBAT_REACH("Reach", true, false, 3.0F, 6.8F, 0.01F),
        CLIENT_COMBAT_FASTPLACE("FastPlace", true, false, 0.0F, 5.0F, 1.0F),
        CLIENT_RENDER_NAMETAG("NameTag", false, true),
        CLIENT_RENDER_VIEWCLIP("ViewClip", false, true),
        CLIENT_PLAYER_AUTOGG("AutoGG", false, true),
        CLIENT_COMBAT_BLOCKINGHIT("BlockingHit", false, true),
        CLIENT_MOVEMENT_NOSLOW("NoSlow", false, true),
        CLIENT_MOVEMENT_EAGLE("Eagle", false, true),
        CLIENT_PLAYER_AUTOTOOL("AutoTool", false, true),
        CLIENT_RENDER_MODULESLIST("ModulesList", false, true),
        CLIENT_MISC_RAWMOUSEINPUT("RawMouseInput", false, true),
        CLIENT_COMBAT_AUTOCLICKER("AutoClicker", true, false, 0.0F, 20.0F, 1.0F),
        CLIENT_PERFORMACNE_CHUNK_UPDATES_LIMIT("ChunkUpdatesLimit", true, false, 1.0F, 250F, 1.0F);
        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;

        Options(String str, boolean isFloat, boolean isBoolean) {
            this(str, isFloat, isBoolean, 0.0F, 1.0F, 0.0F);
        }

        Options(String str, boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep) {
            enumString = str;
            enumFloat = isFloat;
            enumBoolean = isBoolean;
            valueMin = valMin;
            valueMax = valMax;
            valueStep = valStep;
        }

        public static GameSettings.Options getEnumOptions(int ordinal) {
            for (GameSettings.Options gamesettings$options : values()) {
                if (gamesettings$options.returnEnumOrdinal() == ordinal) {
                    return gamesettings$options;
                }
            }

            return null;
        }

        public boolean getEnumFloat() {
            return enumFloat;
        }

        public boolean getEnumBoolean() {
            return enumBoolean;
        }

        public int returnEnumOrdinal() {
            return ordinal();
        }

        public String getEnumString() {
            return enumString;
        }

        public float getValueMax() {
            return valueMax;
        }

        public void setValueMax(float value) {
            valueMax = value;
        }

        public float getValueMin() {
            return valueMin;
        }

        public float normalizeValue(float value) {
            return MathHelper.clamp_float((snapToStepClamp(value) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value) {
            return snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value) {
            value = snapToStep(value);
            return MathHelper.clamp_float(value, valueMin, valueMax);
        }

        private float snapToStep(float value) {
            if (valueStep > 0.0F) {
                value = valueStep * (float) Math.round(value / valueStep);
            }

            return value;
        }
    }
}
