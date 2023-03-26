package net.minecraft.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.gui.*;
import net.optifine.shaders.gui.GuiShaders;

public class GuiVideoSettings extends GuiScreenOF {
    /**
     * An array of all of GameSettings.Options's video options.
     */
    private static final GameSettings.Options[] videoOptions = new GameSettings.Options[]{GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.AO_LEVEL, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.USE_VBO, GameSettings.Options.GAMMA, GameSettings.Options.BLOCK_ALTERNATIVES, GameSettings.Options.DYNAMIC_LIGHTS, GameSettings.Options.DYNAMIC_FOV};
    private final GuiScreen parentGuiScreen;
    private final GameSettings guiGameSettings;
    private final TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    protected String screenTitle = "Video Settings";

    public GuiVideoSettings(GuiScreen parentScreenIn, GameSettings gameSettingsIn) {
        parentGuiScreen = parentScreenIn;
        guiGameSettings = gameSettingsIn;
    }

    public static int getButtonWidth(GuiButton p_getButtonWidth_0_) {
        return p_getButtonWidth_0_.width;
    }

    public static int getButtonHeight(GuiButton p_getButtonHeight_0_) {
        return p_getButtonHeight_0_.height;
    }

    public static void drawGradientRect(GuiScreen p_drawGradientRect_0_, int p_drawGradientRect_1_, int p_drawGradientRect_2_, int p_drawGradientRect_3_, int p_drawGradientRect_4_, int p_drawGradientRect_5_, int p_drawGradientRect_6_) {
        p_drawGradientRect_0_.drawGradientRect(p_drawGradientRect_1_, p_drawGradientRect_2_, p_drawGradientRect_3_, p_drawGradientRect_4_, p_drawGradientRect_5_, p_drawGradientRect_6_);
    }

    public static String getGuiChatText(GuiChat p_getGuiChatText_0_) {
        return p_getGuiChatText_0_.inputField.getText();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        screenTitle = I18n.format("options.videoTitle");
        buttonList.clear();

        for (int i = 0; i < videoOptions.length; ++i) {
            GameSettings.Options gamesettings$options = videoOptions[i];

            int j = width / 2 - 155 + i % 2 * 160;
            int k = height / 6 + 21 * (i / 2) - 12;

            if (gamesettings$options.getEnumFloat()) {
                buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
            } else {
                buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, guiGameSettings.getKeyBinding(gamesettings$options)));
            }
        }

        int l = height / 6 + 21 * (videoOptions.length / 2) - 12;
        int i1;
        i1 = width / 2 - 155;
        buttonList.add(new GuiOptionButton(231, i1, l, Lang.get("of.options.shaders")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(202, i1, l, Lang.get("of.options.quality")));
        l = l + 21;
        i1 = width / 2 - 155;
        buttonList.add(new GuiOptionButton(201, i1, l, Lang.get("of.options.details")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(212, i1, l, Lang.get("of.options.performance")));
        l = l + 21;
        i1 = width / 2 - 155;
        buttonList.add(new GuiOptionButton(211, i1, l, Lang.get("of.options.animations")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(222, i1, l, Lang.get("of.options.other")));
        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168 + 11, I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        actionPerformed(button, 1);
    }

    protected void actionPerformedRightClick(GuiButton p_actionPerformedRightClick_1_) {
        if (p_actionPerformedRightClick_1_.id == GameSettings.Options.GUI_SCALE.ordinal()) {
            actionPerformed(p_actionPerformedRightClick_1_, -1);
        }
    }

    private void actionPerformed(GuiButton p_actionPerformed_1_, int p_actionPerformed_2_) {
        if (p_actionPerformed_1_.enabled) {
            int i = guiGameSettings.guiScale;

            if (p_actionPerformed_1_.id < 200 && p_actionPerformed_1_ instanceof GuiOptionButton) {
                guiGameSettings.setOptionValue(((GuiOptionButton) p_actionPerformed_1_).returnEnumOptions(), p_actionPerformed_2_);
                p_actionPerformed_1_.displayString = guiGameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(p_actionPerformed_1_.id));
            }

            if (p_actionPerformed_1_.id == 200) {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(parentGuiScreen);
            }

            if (guiGameSettings.guiScale != i) {
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int j = scaledresolution.getScaledWidth();
                int k = scaledresolution.getScaledHeight();
                setWorldAndResolution(mc, j, k);
            }

            if (p_actionPerformed_1_.id == 201) {
                mc.gameSettings.saveOptions();
                GuiDetailSettingsOF guidetailsettingsof = new GuiDetailSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guidetailsettingsof);
            }

            if (p_actionPerformed_1_.id == 202) {
                mc.gameSettings.saveOptions();
                GuiQualitySettingsOF guiqualitysettingsof = new GuiQualitySettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiqualitysettingsof);
            }

            if (p_actionPerformed_1_.id == 211) {
                mc.gameSettings.saveOptions();
                GuiAnimationSettingsOF guianimationsettingsof = new GuiAnimationSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guianimationsettingsof);
            }

            if (p_actionPerformed_1_.id == 212) {
                mc.gameSettings.saveOptions();
                GuiPerformanceSettingsOF guiperformancesettingsof = new GuiPerformanceSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiperformancesettingsof);
            }

            if (p_actionPerformed_1_.id == 222) {
                mc.gameSettings.saveOptions();
                GuiOtherSettingsOF guiothersettingsof = new GuiOtherSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiothersettingsof);
            }

            if (p_actionPerformed_1_.id == 231) {
                if (Config.isAntialiasing() || Config.isAntialiasingConfigured()) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.isAnisotropicFiltering()) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
                    return;
                }

                if (Config.isFastRender()) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
                    return;
                }

                if (Config.getGameSettings().anaglyph) {
                    Config.showGuiMessage(Lang.get("of.message.shaders.an1"), Lang.get("of.message.shaders.an2"));
                    return;
                }

                mc.gameSettings.saveOptions();
                GuiShaders guishaders = new GuiShaders(this, guiGameSettings);
                mc.displayGuiScreen(guishaders);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc.theWorld == null) {
            drawDefaultBackground();
        }
        drawCenteredString(fontRendererObj, screenTitle, width / 2, 15, 16777215);
        String s;
        s = "OptiFine HD M6_pre2 Ultra";

        drawString(fontRendererObj, s, 2, height - 10, 8421504);
        String s2 = "OutTouch 1.8.9";
        int i = fontRendererObj.getStringWidth(s2);
        drawString(fontRendererObj, s2, width - i - 2, height - 10, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        tooltipManager.drawTooltips(mouseX, mouseY, buttonList);
    }
}
