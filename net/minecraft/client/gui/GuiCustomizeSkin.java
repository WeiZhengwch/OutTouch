package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiScreenCapeOF;

public class GuiCustomizeSkin extends GuiScreen {
    /**
     * The parent GUI for this GUI
     */
    private final GuiScreen parentScreen;

    /**
     * The title of the GUI.
     */
    private String title;

    public GuiCustomizeSkin(GuiScreen parentScreenIn) {
        parentScreen = parentScreenIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        int i = 0;
        title = I18n.format("options.skinCustomisation.title");

        for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
            buttonList.add(new GuiCustomizeSkin.ButtonPart(enumplayermodelparts.getPartId(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), enumplayermodelparts));
            ++i;
        }

        if (i % 2 == 1) {
            ++i;
        }

        buttonList.add(new GuiButtonOF(210, width / 2 - 100, height / 6 + 24 * (i >> 1), I18n.format("of.options.skinCustomisation.ofCape")));
        i = i + 2;
        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 24 * (i >> 1), I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 210) {
                mc.displayGuiScreen(new GuiScreenCapeOF(this));
            }

            if (button.id == 200) {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(parentScreen);
            } else if (button instanceof GuiCustomizeSkin.ButtonPart) {
                EnumPlayerModelParts enumplayermodelparts = ((GuiCustomizeSkin.ButtonPart) button).playerModelParts;
                mc.gameSettings.switchModelPartEnabled(enumplayermodelparts);
                button.displayString = func_175358_a(enumplayermodelparts);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, title, width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String func_175358_a(EnumPlayerModelParts playerModelParts) {
        String s;

        if (mc.gameSettings.getModelParts().contains(playerModelParts)) {
            s = I18n.format("options.on");
        } else {
            s = I18n.format("options.off");
        }

        return playerModelParts.func_179326_d().getFormattedText() + ": " + s;
    }

    class ButtonPart extends GuiButton {
        private final EnumPlayerModelParts playerModelParts;

        private ButtonPart(int p_i45514_2_, int p_i45514_3_, int p_i45514_4_, EnumPlayerModelParts playerModelParts) {
            super(p_i45514_2_, p_i45514_3_, p_i45514_4_, 150, 20, func_175358_a(playerModelParts));
            this.playerModelParts = playerModelParts;
        }
    }
}
