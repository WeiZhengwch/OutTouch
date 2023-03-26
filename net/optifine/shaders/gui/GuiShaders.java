package net.optifine.shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderEnumShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.config.EnumShaderOption;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class GuiShaders extends GuiScreenOF {
    public static final int EnumOS_UNKNOWN = 0;
    public static final int EnumOS_WINDOWS = 1;
    public static final int EnumOS_OSX = 2;
    public static final int EnumOS_SOLARIS = 3;
    public static final int EnumOS_LINUX = 4;
    private static final float[] QUALITY_MULTIPLIERS = new float[]{0.5F, 0.6F, 0.6666667F, 0.75F, 0.8333333F, 0.9F, 1.0F, 1.1666666F, 1.3333334F, 1.5F, 1.6666666F, 1.8F, 2.0F};
    private static final String[] QUALITY_MULTIPLIER_NAMES = new String[]{"0.5x", "0.6x", "0.66x", "0.75x", "0.83x", "0.9x", "1x", "1.16x", "1.33x", "1.5x", "1.66x", "1.8x", "2x"};
    private static final float QUALITY_MULTIPLIER_DEFAULT = 1.0F;
    private static final float[] HAND_DEPTH_VALUES = new float[]{0.0625F, 0.125F, 0.25F};
    private static final String[] HAND_DEPTH_NAMES = new String[]{"0.5x", "1x", "2x"};
    private static final float HAND_DEPTH_DEFAULT = 0.125F;
    private final TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderEnumShaderOptions());
    protected GuiScreen parentGui;
    protected String screenTitle = "Shaders";
    private int updateTimer = -1;
    private GuiSlotShaders shaderList;
    private boolean saved;

    public GuiShaders(GuiScreen par1GuiScreen, GameSettings par2GameSettings) {
        parentGui = par1GuiScreen;
    }

    public static String toStringOnOff(boolean value) {
        String s = Lang.getOn();
        String s1 = Lang.getOff();
        return value ? s : s1;
    }

    public static String toStringAa(int value) {
        return value == 2 ? "FXAA 2x" : (value == 4 ? "FXAA 4x" : Lang.getOff());
    }

    public static String toStringValue(float val, float[] values, String[] names) {
        int i = getValueIndex(val, values);
        return names[i];
    }

    public static int getValueIndex(float val, float[] values) {
        for (int i = 0; i < values.length; ++i) {
            float f = values[i];

            if (f >= val) {
                return i;
            }
        }

        return values.length - 1;
    }

    public static String toStringQuality(float val) {
        return toStringValue(val, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_NAMES);
    }

    public static String toStringHandDepth(float val) {
        return toStringValue(val, HAND_DEPTH_VALUES, HAND_DEPTH_NAMES);
    }

    public static int getOSType() {
        String s = System.getProperty("os.name").toLowerCase();
        return s.contains("win") ? 1 : (s.contains("mac") ? 2 : (s.contains("solaris") ? 3 : (s.contains("sunos") ? 3 : (s.contains("linux") ? 4 : (s.contains("unix") ? 4 : 0)))));
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        screenTitle = I18n.format("of.options.shadersTitle");

        if (Shaders.shadersConfig == null) {
            Shaders.loadConfig();
        }

        int i = 120;
        int j = 20;
        int k = width - i - 10;
        int l = 30;
        int i1 = 20;
        int j1 = width - i - 20;
        shaderList = new GuiSlotShaders(this, j1, height, l, height - 50, 16);
        shaderList.registerScrollButtons(7, 8);
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.ANTIALIASING, k, l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.NORMAL_MAP, k, i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SPECULAR_MAP, k, 2 * i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.RENDER_RES_MUL, k, 3 * i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SHADOW_RES_MUL, k, 4 * i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.HAND_DEPTH_MUL, k, 5 * i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_HAND_LIGHT, k, 6 * i1 + l, i, j));
        buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_LIGHTING, k, 7 * i1 + l, i, j));
        int k1 = Math.min(150, j1 / 2 - 10);
        int l1 = j1 / 4 - k1 / 2;
        int i2 = height - 25;
        buttonList.add(new GuiButton(201, l1, i2, k1 - 22 + 1, j, Lang.get("of.options.shaders.shadersFolder")));
        buttonList.add(new GuiButtonDownloadShaders(210, l1 + k1 - 22 - 1, i2));
        buttonList.add(new GuiButton(202, j1 / 4 * 3 - k1 / 2, height - 25, k1, j, I18n.format("gui.done")));
        buttonList.add(new GuiButton(203, k, height - 25, i, j, Lang.get("of.options.shaders.shaderOptions")));
        updateButtons();
    }

    public void updateButtons() {
        boolean flag = Config.isShaders();

        for (GuiButton guibutton : buttonList) {
            if (guibutton.id != 201 && guibutton.id != 202 && guibutton.id != 210 && guibutton.id != EnumShaderOption.ANTIALIASING.ordinal()) {
                guibutton.enabled = flag;
            }
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        shaderList.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        actionPerformed(button, false);
    }

    protected void actionPerformedRightClick(GuiButton button) {
        actionPerformed(button, true);
    }

    private void actionPerformed(GuiButton button, boolean rightClick) {
        if (button.enabled) {
            if (!(button instanceof GuiButtonEnumShaderOption guibuttonenumshaderoption)) {
                if (!rightClick) {
                    switch (button.id) {
                        case 201:
                            switch (getOSType()) {
                                case 1:
                                    String s = String.format("cmd.exe /C start \"Open file\" \"%s\"", Shaders.shaderPacksDir.getAbsolutePath());

                                    try {
                                        Runtime.getRuntime().exec(s);
                                        return;
                                    } catch (IOException ioexception) {
                                        ioexception.printStackTrace();
                                        break;
                                    }

                                case 2:
                                    try {
                                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", Shaders.shaderPacksDir.getAbsolutePath()});
                                        return;
                                    } catch (IOException ioexception1) {
                                        ioexception1.printStackTrace();
                                    }
                            }

                            boolean flag = false;

                            try {
                                Class oclass1 = Class.forName("java.awt.Desktop");
                                Object object1 = oclass1.getMethod("getDesktop", new Class[0]).invoke(null);
                                oclass1.getMethod("browse", new Class[]{URI.class}).invoke(object1, (new File(mc.mcDataDir, "shaderpacks")).toURI());
                            } catch (Throwable throwable1) {
                                throwable1.printStackTrace();
                                flag = true;
                            }

                            if (flag) {
                                Config.dbg("Opening via system class!");
                                Sys.openURL("file://" + Shaders.shaderPacksDir.getAbsolutePath());
                            }

                            break;

                        case 202:
                            Shaders.storeConfig();
                            saved = true;
                            mc.displayGuiScreen(parentGui);
                            break;

                        case 203:
                            GuiShaderOptions guishaderoptions = new GuiShaderOptions(this, Config.getGameSettings());
                            Config.getMinecraft().displayGuiScreen(guishaderoptions);
                            break;

                        case 210:
                            try {
                                Class<?> oclass = Class.forName("java.awt.Desktop");
                                Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
                                oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new URI("http://optifine.net/shaderPacks"));
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }

                        case 204:
                        case 205:
                        case 206:
                        case 207:
                        case 208:
                        case 209:
                        default:
                            shaderList.actionPerformed(button);
                    }
                }
            } else {

                switch (guibuttonenumshaderoption.getEnumShaderOption()) {
                    case ANTIALIASING -> {
                        Shaders.nextAntialiasingLevel(!rightClick);
                        if (hasShiftDown()) {
                            Shaders.configAntialiasingLevel = 0;
                        }
                        Shaders.uninit();
                    }
                    case NORMAL_MAP -> {
                        Shaders.configNormalMap = !Shaders.configNormalMap;
                        if (hasShiftDown()) {
                            Shaders.configNormalMap = true;
                        }
                        Shaders.uninit();
                        mc.scheduleResourcesRefresh();
                    }
                    case SPECULAR_MAP -> {
                        Shaders.configSpecularMap = !Shaders.configSpecularMap;
                        if (hasShiftDown()) {
                            Shaders.configSpecularMap = true;
                        }
                        Shaders.uninit();
                        mc.scheduleResourcesRefresh();
                    }
                    case RENDER_RES_MUL -> {
                        Shaders.configRenderResMul = getNextValue(Shaders.configRenderResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, hasShiftDown());
                        Shaders.uninit();
                        Shaders.scheduleResize();
                    }
                    case SHADOW_RES_MUL -> {
                        Shaders.configShadowResMul = getNextValue(Shaders.configShadowResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, hasShiftDown());
                        Shaders.uninit();
                        Shaders.scheduleResizeShadow();
                    }
                    case HAND_DEPTH_MUL -> {
                        Shaders.configHandDepthMul = getNextValue(Shaders.configHandDepthMul, HAND_DEPTH_VALUES, HAND_DEPTH_DEFAULT, !rightClick, hasShiftDown());
                        Shaders.uninit();
                    }
                    case OLD_HAND_LIGHT -> {
                        Shaders.configOldHandLight.nextValue(!rightClick);
                        if (hasShiftDown()) {
                            Shaders.configOldHandLight.resetValue();
                        }
                        Shaders.uninit();
                    }
                    case OLD_LIGHTING -> {
                        Shaders.configOldLighting.nextValue(!rightClick);
                        if (hasShiftDown()) {
                            Shaders.configOldLighting.resetValue();
                        }
                        Shaders.updateBlockLightLevel();
                        Shaders.uninit();
                        mc.scheduleResourcesRefresh();
                    }
                    case TWEAK_BLOCK_DAMAGE -> Shaders.configTweakBlockDamage = !Shaders.configTweakBlockDamage;
                    case CLOUD_SHADOW -> Shaders.configCloudShadow = !Shaders.configCloudShadow;
                    case TEX_MIN_FIL_B -> {
                        Shaders.configTexMinFilB = (Shaders.configTexMinFilB + 1) % 3;
                        Shaders.configTexMinFilN = Shaders.configTexMinFilS = Shaders.configTexMinFilB;
                        button.displayString = "Tex Min: " + Shaders.texMinFilDesc[Shaders.configTexMinFilB];
                        ShadersTex.updateTextureMinMagFilter();
                    }
                    case TEX_MAG_FIL_N -> {
                        Shaders.configTexMagFilN = (Shaders.configTexMagFilN + 1) % 2;
                        button.displayString = "Tex_n Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilN];
                        ShadersTex.updateTextureMinMagFilter();
                    }
                    case TEX_MAG_FIL_S -> {
                        Shaders.configTexMagFilS = (Shaders.configTexMagFilS + 1) % 2;
                        button.displayString = "Tex_s Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilS];
                        ShadersTex.updateTextureMinMagFilter();
                    }
                    case SHADOW_CLIP_FRUSTRUM -> {
                        Shaders.configShadowClipFrustrum = !Shaders.configShadowClipFrustrum;
                        button.displayString = "ShadowClipFrustrum: " + toStringOnOff(Shaders.configShadowClipFrustrum);
                        ShadersTex.updateTextureMinMagFilter();
                    }
                }

                guibuttonenumshaderoption.updateButtonText();
            }
        }
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        super.onGuiClosed();

        if (!saved) {
            Shaders.storeConfig();
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        shaderList.drawScreen(mouseX, mouseY, partialTicks);

        if (updateTimer <= 0) {
            shaderList.updateList();
            updateTimer += 20;
        }

        drawCenteredString(fontRendererObj, screenTitle + " ", width / 2, 15, 16777215);
        String s = "OpenGL: " + Shaders.glVersionString + ", " + Shaders.glVendorString + ", " + Shaders.glRendererString;
        int i = fontRendererObj.getStringWidth(s);

        if (i < width - 5) {
            drawCenteredString(fontRendererObj, s, width / 2, height - 40, 8421504);
        } else {
            drawString(fontRendererObj, s, 5, height - 40, 8421504);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        tooltipManager.drawTooltips(mouseX, mouseY, buttonList);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();
        --updateTimer;
    }

    public Minecraft getMc() {
        return mc;
    }

    public void drawCenteredString(String text, int x, int y, int color) {
        drawCenteredString(fontRendererObj, text, x, y, color);
    }

    private float getNextValue(float val, float[] values, float valDef, boolean forward, boolean reset) {
        if (reset) {
            return valDef;
        } else {
            int i = getValueIndex(val, values);

            if (forward) {
                ++i;

                if (i >= values.length) {
                    i = 0;
                }
            } else {
                --i;

                if (i < 0) {
                    i = values.length - 1;
                }
            }

            return values[i];
        }
    }

    public boolean hasShiftDown() {
        return isShiftKeyDown();
    }
}
