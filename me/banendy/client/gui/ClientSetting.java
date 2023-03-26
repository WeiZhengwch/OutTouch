package me.banendy.client.gui;

import me.banendy.client.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.optifine.gui.GuiOptionButtonOF;
import net.optifine.gui.GuiOptionSliderOF;

public class ClientSetting extends GuiScreen {
    private static final GameSettings.Options[] enumOptions = new GameSettings.Options[]{GameSettings.Options.CLIENT_COMBAT_VELOCITY_VERT, GameSettings.Options.CLIENT_COMBAT_VELOCITY_HORI, GameSettings.Options.CLIENT_COMBAT_REACH, GameSettings.Options.CLIENT_COMBAT_FASTPLACE, GameSettings.Options.CLIENT_COMBAT_AUTOCLICKER, GameSettings.Options.CLIENT_RENDER_NAMETAG, GameSettings.Options.CLIENT_RENDER_VIEWCLIP, GameSettings.Options.CLIENT_PLAYER_AUTOGG, GameSettings.Options.CLIENT_MOVEMENT_NOSLOW, GameSettings.Options.CLIENT_COMBAT_BLOCKINGHIT, GameSettings.Options.CLIENT_MOVEMENT_EAGLE, GameSettings.Options.CLIENT_PLAYER_AUTOTOOL, GameSettings.Options.CLIENT_RENDER_MODULESLIST};
    private static final byte[] BYTES = new byte[0];
    private final GuiScreen prevScreen;
    private final GameSettings settings;
    Minecraft mc = Minecraft.getMinecraft();

    public ClientSetting(GuiScreen guiscreen, GameSettings gamesettings) {
        prevScreen = guiscreen;
        settings = gamesettings;
    }

    public void initGui() {
        buttonList.clear();
        int i = 0;

        for (GameSettings.Options gamesettings$options : enumOptions) {
            int j = width / 2 - 155 + i % 2 * 160;
            int k = height / 6 + 21 * (i / 2) - 12;
            if (!gamesettings$options.getEnumFloat()) {

                buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, settings.getKeyBinding(gamesettings$options)));
            } else {
                buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
            }

            ++i;
        }
        buttonList.add(new GuiButton(201, width / 2 - 100, height / 6 + 168 + 11, I18n.format("gui.done")));
        buttonList.add(new GuiButton(202, 0, height - 20, 20, 20, I18n.format("GC")));
        buttonList.add(new GuiButton(203, 20, height - 20, 50, 20, I18n.format("Fully GC")));
        buttonList.add(new GuiButton(204, 0, height - 40, 20, 20, I18n.format("1")));
        buttonList.add(new GuiButton(205, 20, height - 40, 20, 20, I18n.format("2")));
        buttonList.add(new GuiButton(206, 40, height - 40, 20, 20, I18n.format("3")));
        buttonList.add(new GuiButton(207, 70, height - 20, 40, 20, I18n.format("Reload")));
    }

    public void confirmClicked(boolean result, int id) {
        mc.displayGuiScreen(this);
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id < 200 && button instanceof GuiOptionButton) {
            settings.setOptionValue(((GuiOptionButton) button).returnEnumOptions(), 1);
            button.displayString = settings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        }
        switch (button.id) {
            case 201 -> {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(prevScreen);
            }
            case 202 -> System.gc();
            case 203 -> {
                Minecraft.memoryReserve = BYTES;
                mc.renderGlobal.deleteAllDisplayLists();
                System.gc();
            }
            case 204 -> GuiScreen.style = 0;
            case 205 -> GuiScreen.style = 1;
            case 206 -> GuiScreen.style = 2;
            case 207 -> {
                MainClient.loader();
                MainMenu.fadeout = 63;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawWorldBackground(0);
        mc.ingameGUI.drawCenteredString(fontRendererObj, "Options", width / 2, 15, 16777215);
        drawEridani(true, 0, height, 120, 195);
        drawAsphodene(true, width - 120, height, 120, 195);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
