package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("ALL")
public class GuiChat extends GuiScreen {
    private final List<String> foundPlayerNames = Lists.newArrayList();
    /**
     * Chat entry field
     */
    protected GuiTextField inputField;
    int k = 0;
    private String historyBuffer = "";
    /**
     * keeps position of which chat message you will select when you press up, (does not increase for duplicated
     * messages sent immediately after each other)
     */
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    /**
     * is the text that appears when you press the chat key and the input box appears pre-filled
     */
    private String defaultInputFieldText = "";

    public GuiChat() {
    }

    public GuiChat(String defaultText) {
        defaultInputFieldText = defaultText;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        sentHistoryCursor = mc.ingameGUI.getChatGUI().getSentMessages().size();
        inputField = new GuiTextField(0, fontRendererObj, 4, height - 12, width - 4, 12);
        inputField.setMaxStringLength(100);
        inputField.setEnableBackgroundDrawing(false);
        inputField.setFocused(true);
        inputField.setText(defaultInputFieldText);
        inputField.setCanLoseFocus(false);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        mc.ingameGUI.getChatGUI().resetScroll();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        inputField.updateCursorCounter();
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        waitingOnAutocomplete = false;

        if (keyCode == 15) {
            autocompletePlayerNames();
        } else {
            playerNamesFound = false;
        }

        if (keyCode == 1) {
            mc.displayGuiScreen(null);
        } else if (keyCode != 28 && keyCode != 156) {
            switch (keyCode) {
                case 200 -> getSentHistory(-1);
                case 208 -> getSentHistory(1);
                case 201 -> mc.ingameGUI.getChatGUI().scroll(mc.ingameGUI.getChatGUI().getLineCount() - 1);
                case 209 -> mc.ingameGUI.getChatGUI().scroll(-mc.ingameGUI.getChatGUI().getLineCount() + 1);
                default -> inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = inputField.getText().trim();

            if (isShiftKeyDown()) {
                sendChatMessage(s);
                inputField.setText("");

            } else if (s.length() > 0) {
                sendChatMessage(s);
                mc.displayGuiScreen(null);
            }
        }
    }

    /**
     * Handles mouse input.
     */
    public float scrolled;
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            if (i > 0) {
                i = -1;
                scrolled--;
            } else {
                i = 1;
                scrolled++;
            }

            if (!isShiftKeyDown()) {
                i *= 7;
            }

        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            IChatComponent ichatcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (handleComponentClick(ichatcomponent)) {
                return;
            }
        }

        inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Sets the text of the chat
     */
    protected void setText(String newChatText, boolean shouldOverwrite) {
        if (shouldOverwrite) {
            inputField.setText(newChatText);
        } else {
            inputField.writeText(newChatText);
        }
    }

    public void autocompletePlayerNames() {
        if (playerNamesFound) {
            inputField.deleteFromCursor(inputField.func_146197_a(-1, inputField.getCursorPosition(), false) - inputField.getCursorPosition());

            if (autocompleteIndex >= foundPlayerNames.size()) {
                autocompleteIndex = 0;
            }
        } else {
            int i = inputField.func_146197_a(-1, inputField.getCursorPosition(), false);
            foundPlayerNames.clear();
            autocompleteIndex = 0;
            String s1 = inputField.getText().substring(0, inputField.getCursorPosition());
            sendAutocompleteRequest(s1);

            if (foundPlayerNames.isEmpty()) {
                return;
            }

            playerNamesFound = true;
            inputField.deleteFromCursor(i - inputField.getCursorPosition());
        }

        if (foundPlayerNames.size() > 1) {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : foundPlayerNames) {
                if (stringbuilder.length() > 0) {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(s2);
            }

            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        inputField.writeText(foundPlayerNames.get(autocompleteIndex++));
    }

    private void sendAutocompleteRequest(String p_146405_1_) {
        if (p_146405_1_.length() >= 1) {
            BlockPos blockpos = null;

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                blockpos = mc.objectMouseOver.getBlockPos();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_, blockpos));
            waitingOnAutocomplete = true;
        }
    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
     * message from the current cursor position
     */
    public void getSentHistory(int msgPos) {
        int i = sentHistoryCursor + msgPos;
        int j = mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != sentHistoryCursor) {
            if (i == j) {
                sentHistoryCursor = j;
                inputField.setText(historyBuffer);
            } else {
                if (sentHistoryCursor == j) {
                    historyBuffer = inputField.getText();
                }

                inputField.setText(mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                sentHistoryCursor = i;
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (scrolled > -0.05 && scrolled < 0.05) {
            scrolled = 0;
        } else {
            scrolled *= 0.875f;
        }
        mc.ingameGUI.getChatGUI().scroll(scrolled);
        if (k * k <= width) {
            ++k;
        }
        drawRect(0, height - 22, k * k, height, Integer.MIN_VALUE);
        drawRect(0, height - 22, 2, height, new Color(0, 111, 255).getRGB());
        inputField.drawTextBox();
        IChatComponent ichatcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
            handleComponentHover(ichatcomponent, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void onAutocompleteResponse(String[] p_146406_1_) {
        if (waitingOnAutocomplete) {
            playerNamesFound = false;
            foundPlayerNames.clear();

            for (String s : p_146406_1_) {
                if (s.length() > 0) {
                    foundPlayerNames.add(s);
                }
            }

            String s1 = inputField.getText().substring(inputField.func_146197_a(-1, inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(p_146406_1_);

            if (s2.length() > 0 && !s1.equalsIgnoreCase(s2)) {
                inputField.deleteFromCursor(inputField.func_146197_a(-1, inputField.getCursorPosition(), false) - inputField.getCursorPosition());
                inputField.writeText(s2);
            } else if (foundPlayerNames.size() > 0) {
                playerNamesFound = true;
                autocompletePlayerNames();
            }
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }
}
