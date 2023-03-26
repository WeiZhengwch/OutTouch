package net.optifine.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;

import java.util.List;

public class GuiMessage extends GuiScreen {
    private final GuiScreen parentScreen;
    private final String messageLine1;
    private final String messageLine2;
    private final List listLines2 = Lists.newArrayList();
    protected String confirmButtonText;
    private int ticksUntilEnable;

    public GuiMessage(GuiScreen parentScreen, String line1, String line2) {
        this.parentScreen = parentScreen;
        messageLine1 = line1;
        messageLine2 = line2;
        confirmButtonText = I18n.format("gui.done");
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        buttonList.add(new GuiOptionButton(0, width / 2 - 74, height / 6 + 96, confirmButtonText));
        listLines2.clear();
        listLines2.addAll(fontRendererObj.listFormattedStringToWidth(messageLine2, width - 50));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        Config.getMinecraft().displayGuiScreen(parentScreen);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawCenteredString(fontRendererObj, messageLine1, width / 2, 70, 16777215);
        int i = 90;

        for (Object s : listLines2) {
            drawCenteredString(fontRendererObj, (String) s, width / 2, i, 16777215);
            i += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void setButtonDelay(int ticksUntilEnable) {
        this.ticksUntilEnable = ticksUntilEnable;

        for (GuiButton guibutton : buttonList) {
            guibutton.enabled = false;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();

        if (--ticksUntilEnable == 0) {
            for (GuiButton guibutton : buttonList) {
                guibutton.enabled = true;
            }
        }
    }
}
