package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiPerformanceSettingsOF extends GuiScreen {
    private static final GameSettings.Options[] enumOptions = new GameSettings.Options[]{GameSettings.Options.SMOOTH_FPS, GameSettings.Options.SMOOTH_WORLD, GameSettings.Options.FAST_RENDER, GameSettings.Options.FAST_MATH, GameSettings.Options.CHUNK_UPDATES, GameSettings.Options.CHUNK_UPDATES_DYNAMIC, GameSettings.Options.RENDER_REGIONS, GameSettings.Options.LAZY_CHUNK_LOADING, GameSettings.Options.SMART_ANIMATIONS, GameSettings.Options.CLIENT_PERFORMACNE_CHUNK_UPDATES_LIMIT};
    private final GuiScreen prevScreen;
    private final GameSettings settings;
    private final TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    protected String title;

    public GuiPerformanceSettingsOF(GuiScreen guiscreen, GameSettings gamesettings) {
        prevScreen = guiscreen;
        settings = gamesettings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        title = I18n.format("of.options.performanceTitle");
        buttonList.clear();

        for (int i = 0; i < enumOptions.length; ++i) {
            GameSettings.Options gamesettings$options = enumOptions[i];
            int j = width / 2 - 155 + i % 2 * 160;
            int k = height / 6 + 21 * (i / 2) - 12;

            if (!gamesettings$options.getEnumFloat()) {
                buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, settings.getKeyBinding(gamesettings$options)));
            } else {
                buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
            }
        }

        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168 + 11, I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.enabled) {
            if (guibutton.id < 200 && guibutton instanceof GuiOptionButton) {
                settings.setOptionValue(((GuiOptionButton) guibutton).returnEnumOptions(), 1);
                guibutton.displayString = settings.getKeyBinding(GameSettings.Options.getEnumOptions(guibutton.id));
            }

            if (guibutton.id == 200) {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(prevScreen);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int x, int y, float f) {
        drawWorldBackground(0);
        drawCenteredString(fontRendererObj, title, width / 2, 15, 16777215);
        super.drawScreen(x, y, f);

        tooltipManager.drawTooltips(x, y, buttonList);
    }
}
