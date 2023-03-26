package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityList;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ALL")
public abstract class GuiScreen extends Gui implements GuiYesNoCallback {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
    private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
    /**
     * The width of the screen object.
     */
    public int width;
    /**
     * The height of the screen object.
     */
    public int height;
    public boolean allowUserInput;
    /**
     * Reference to the Minecraft object.
     */
    protected Minecraft mc;
    /**
     * Holds a instance of RenderItem, used to draw the achievement icons on screen (is based on ItemStack)
     */
    protected RenderItem itemRender;
    protected List<GuiButton> buttonList = Lists.newArrayList();
    protected List<GuiLabel> labelList = Lists.newArrayList();
    /**
     * The FontRenderer used by GuiScreen
     */
    protected FontRenderer fontRendererObj;

    /**
     * The button that was just pressed.
     */
    private GuiButton selectedButton;
    private int eventButton;
    private long lastMouseEvent;

    /**
     * Tracks the number of fingers currently on the screen. Prevents subsequent fingers registering as clicks.
     */
    private int touchValue;
    private URI clickedLinkURI;

    /**
     * Returns a string stored in the system clipboard.
     */
    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    /**
     * Stores the given string in the system clipboard
     */
    public static void setClipboardString(String copyText) {
        if (!StringUtils.isEmpty(copyText)) {
            try {
                StringSelection stringselection = new StringSelection(copyText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
            } catch (Exception var2) {
            }
        }
    }

    /**
     * Returns true if either windows ctrl key is down or if either mac meta key is down
     */
    public static boolean isCtrlKeyDown() {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    /**
     * Returns true if either shift key is down
     */
    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    /**
     * Returns true if either alt key is down
     */
    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    public static boolean isKeyComboCtrlX(int keyID) {
        return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlV(int keyID) {
        return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlC(int keyID) {
        return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlA(int keyID) {
        return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (GuiButton guiButton : buttonList) {
            guiButton.drawButton(mc, mouseX, mouseY);
        }

        for (GuiLabel guiLabel : labelList) {
            guiLabel.drawLabel(mc, mouseX, mouseY);
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);

            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        }
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        List<String> list = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(0, stack.getRarity().rarityColor + list.get(i));
            } else {
                list.set(i, EnumChatFormatting.GRAY + list.get(i));
            }
        }

        drawHoveringText(list, x, y);
    }

    /**
     * Draws the text when mouse is over creative inventory tab. Params: current creative tab to be checked, current
     * mouse x position, current mouse y position.
     */
    protected void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY) {
        drawHoveringText(Collections.singletonList(tabName), mouseX, mouseY);
    }

    /**
     * Draws a List of strings as a tooltip. Every entry is drawn on a seperate line.
     */
    protected void drawHoveringText(List<String> textLines, int x, int y) {
        if (!textLines.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int i = 0;

            for (String s : textLines) {
                int j = fontRendererObj.getStringWidth(s);

                if (j > i) {
                    i = j;
                }
            }

            int l1 = x + 12;
            int i2 = y - 12;
            int k = 8;

            if (textLines.size() > 1) {
                k += 2 + (textLines.size() - 1) * 10;
            }

            if (l1 + i > width) {
                l1 -= 28 + i;
            }

            if (i2 + k + 6 > height) {
                i2 = height - k - 6;
            }

            zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int l = -267386864;
            drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
            drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
            drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
            drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
            drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
            int i1 = 1347420415;
            int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
            drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
            drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
            drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
            drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);

            for (int k1 = 0; k1 < textLines.size(); ++k1) {
                String s1 = textLines.get(k1);
                fontRendererObj.drawStringWithShadow(s1, (float) l1, (float) i2, -1);

                if (k1 == 0) {
                    i2 += 2;
                }

                i2 += 10;
            }

            zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    /**
     * Draws the hover event specified by the given chat component
     *
     * @param component The IChatComponent to render
     * @param x         The x position where to render
     * @param y         The y position where to render
     */
    protected void handleComponentHover(IChatComponent component, int x, int y) {
        if (component != null && component.getChatStyle().getChatHoverEvent() != null) {
            HoverEvent hoverevent = component.getChatStyle().getChatHoverEvent();

            if (hoverevent.getAction() == HoverEvent.Action.SHOW_ITEM) {
                ItemStack itemstack = null;

                try {
                    NBTTagCompound nbtbase = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());

                    if (nbtbase instanceof NBTTagCompound) {
                        itemstack = ItemStack.loadItemStackFromNBT(nbtbase);
                    }
                } catch (NBTException var11) {
                }

                if (itemstack != null) {
                    renderToolTip(itemstack, x, y);
                } else {
                    drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Item!", x, y);
                }
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
                if (mc.gameSettings.advancedItemTooltips) {
                    try {
                        NBTBase nbtbase1 = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());

                        if (nbtbase1 instanceof NBTTagCompound nbttagcompound) {
                            List<String> list1 = Lists.newArrayList();
                            list1.add(nbttagcompound.getString("name"));

                            if (nbttagcompound.hasKey("type", 8)) {
                                String s = nbttagcompound.getString("type");
                                list1.add("Type: " + s + " (" + EntityList.getIDFromString(s) + ")");
                            }

                            list1.add(nbttagcompound.getString("id"));
                            drawHoveringText(list1, x, y);
                        } else {
                            drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Entity!", x, y);
                        }
                    } catch (NBTException var10) {
                        drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Entity!", x, y);
                    }
                }
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                drawHoveringText(NEWLINE_SPLITTER.splitToList(hoverevent.getValue().getFormattedText()), x, y);
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
                StatBase statbase = StatList.getOneShotStat(hoverevent.getValue().getUnformattedText());

                if (statbase != null) {
                    IChatComponent ichatcomponent = statbase.getStatName();
                    IChatComponent ichatcomponent1 = new ChatComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"));
                    ichatcomponent1.getChatStyle().setItalic(Boolean.TRUE);
                    String s1 = statbase instanceof Achievement ? ((Achievement) statbase).getDescription() : null;
                    List<String> list = Lists.newArrayList(ichatcomponent.getFormattedText(), ichatcomponent1.getFormattedText());

                    if (s1 != null) {
                        list.addAll(fontRendererObj.listFormattedStringToWidth(s1, 150));
                    }

                    drawHoveringText(list, x, y);
                } else {
                    drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid statistic/achievement!", x, y);
                }
            }

            GlStateManager.disableLighting();
        }
    }

    /**
     * Sets the text of the chat
     */
    protected void setText(String newChatText, boolean shouldOverwrite) {
    }

    /**
     * Executes the click event specified by the given chat component
     *
     * @param component The ChatComponent to check for click
     */
    protected boolean handleComponentClick(IChatComponent component) {
        if (component == null) {
            return false;
        } else {
            ClickEvent clickevent = component.getChatStyle().getChatClickEvent();

            if (isShiftKeyDown()) {
                if (component.getChatStyle().getInsertion() != null) {
                    setText(component.getChatStyle().getInsertion(), false);
                }
            } else if (clickevent != null) {
                if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!mc.gameSettings.chatLinks) {
                        return false;
                    }

                    try {
                        URI uri = new URI(clickevent.getValue());
                        String s = uri.getScheme();

                        if (s == null) {
                            throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                        }

                        if (!PROTOCOLS.contains(s.toLowerCase())) {
                            throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase());
                        }

                        if (mc.gameSettings.chatLinksPrompt) {
                            clickedLinkURI = uri;
                            mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 31102009, false));
                        } else {
                            openWebLink(uri);
                        }
                    } catch (URISyntaxException urisyntaxexception) {
                        LOGGER.error("Can't open url for " + clickevent, urisyntaxexception);
                    }
                } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
                    URI uri1 = (new File(clickevent.getValue())).toURI();
                    openWebLink(uri1);
                } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    setText(clickevent.getValue(), true);
                } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    sendChatMessage(clickevent.getValue(), false);
                } else {
                    LOGGER.error("Don't know how to handle " + clickevent);
                }

                return true;
            }

            return false;
        }
    }

    /**
     * Used to add chat messages to the client's GuiChat.
     */
    public void sendChatMessage(String msg) {
        sendChatMessage(msg, true);
    }

    public void sendChatMessage(String msg, boolean addToChat) {
        if (addToChat) {
            mc.ingameGUI.getChatGUI().addToSentMessages(msg);
        }

        mc.thePlayer.sendChatMessage(msg);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (int i = 0; i < this.buttonList.size(); ++i) {
                GuiButton guibutton = (GuiButton) this.buttonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                }
            }
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (selectedButton != null && state == 0) {
            selectedButton.mouseReleased(mouseX, mouseY);
            selectedButton = null;
        }
    }

    /**
     * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
     * lastButtonClicked & timeSinceMouseClick.
     */
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
    }

    /**
     * Causes the screen to lay out its subcomponents again. This is the equivalent of the Java call
     * Container.validate()
     */
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        itemRender = mc.getRenderItem();
        fontRendererObj = mc.fontRendererObj;
        this.width = width;
        this.height = height;
        buttonList.clear();
        initGui();
    }

    /**
     * Set the gui to the specified width and height
     *
     * @param w The width of the screen
     * @param h The height of the screen
     */
    public void setGuiSize(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
    }

    /**
     * Delegates mouse and keyboard input.
     */
    public void handleInput() throws IOException {
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
                handleMouseInput();
            }
        }

        if (Keyboard.isCreated()) {
            while (Keyboard.next()) {
                handleKeyboardInput();
            }
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();

        if (Mouse.getEventButtonState()) {
            if (mc.gameSettings.touchscreen && touchValue++ > 0) {
                return;
            }

            eventButton = k;
            lastMouseEvent = Minecraft.getSystemTime();
            mouseClicked(i, j, eventButton);
        } else if (k != -1) {
            if (mc.gameSettings.touchscreen && --touchValue > 0) {
                return;
            }

            eventButton = -1;
            mouseReleased(i, j, k);
        } else if (eventButton != -1 && lastMouseEvent > 0L) {
            long l = Minecraft.getSystemTime() - lastMouseEvent;
            mouseClickMove(i, j, eventButton, l);
        }
    }

    /**
     * Handles keyboard input.
     */
    public void handleKeyboardInput() throws IOException {
        int k = Keyboard.getEventKey();
        char c = Keyboard.getEventCharacter();

        if (Keyboard.getEventKeyState() || k == 0 && Character.isDefined(c)) {
            keyTyped(c, k);
        }

    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
    }

    /**
     * Draws either a gradient over the background screen (when it exists) or a flat gradient over background.png
     */
    public void drawDefaultBackground() {
        drawWorldBackground(0);
    }

    public void drawWorldBackground(int tint) {
        if (mc.theWorld == null) {
            drawBackground(tint);
        }
    }

    /**
     * Draws the background (i is always 0 as of 1.2.2)
     */
    public void drawBackground(int tint) {
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        if (tint == 1) {
            mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/xibao.png"));
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
        } else {
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/MenuBackground.png"));
            Gui.drawModalRectWithCustomSizedTexture((int) (-org.lwjgl.input.Mouse.getX() * 0.15), 0, 0, 0, 3840, 2160, width + 200, height);
            mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/SeaOfStarsBGFar.png"));
            Gui.drawModalRectWithCustomSizedTexture((int) (-org.lwjgl.input.Mouse.getX() * 0.10) - 300, 0, 0, 0, 1024, 408, width + 200, height);
            mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/StarsAboveLogo.png"));
            Gui.drawModalRectWithCustomSizedTexture(width / 2 - 64, -7, 0, 0, 128, 128, 128, 128);
        }
        if (tint == 2) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/options_background.png"));
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, 16, 16);
            Gui.drawRect(0, 0, width, height, Integer.MIN_VALUE);
        }
        bufferbuilder.begin(0, DefaultVertexFormats.POSITION_TEX_COLOR);
        tessellator.draw();
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return true;
    }

    public void confirmClicked(boolean result, int id) {
        if (id == 31102009) {
            if (result) {
                openWebLink(clickedLinkURI);
            }

            clickedLinkURI = null;
            mc.displayGuiScreen(this);
        }
    }

    private void openWebLink(URI url) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke(null);
            oclass.getMethod("browse", URI.class).invoke(object, url);
        } catch (Throwable throwable) {
            LOGGER.error("Couldn't open link", throwable);
        }
    }

    /**
     * Called when the GUI is resized in order to update the world and the resolution
     *
     * @param w The width of the screen
     * @param h The height of the screen
     */
    public void onResize(Minecraft mcIn, int w, int h) {
        setWorldAndResolution(mcIn, w, h);
    }

    int lastcanupdate;
    int nowcanupdate;
    int lastcanupdate1;
    int nowcanupdate1;
    int i5 = 0;
    int i6 = 0;

    /**
     * Draws Asphodene with shadow on the screen. Args : shadow, x, y, texturewidth, textureheight (Default = 120 * 195).
     */
    public void drawAsphodene(boolean Shadow, int x, int y, int texturewidth, int textureheight) {
        nowcanupdate = Minecraft.getMinecraft().Delay;
        if (Shadow) {
            mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/AsphodeneS.png"));
            if (i5 < 4 && nowcanupdate > lastcanupdate) {
                i5++;
                lastcanupdate = nowcanupdate;
            } else if (i5 > 4) {
                i5--;
            }
            Gui.drawModalRectWithCustomSizedTexture(x + i5, y / 2 - textureheight / 2, 0, 0, texturewidth, textureheight, texturewidth, textureheight);
        }
        mc.getTextureManager().bindTexture(new ResourceLocation("outtouch/Asphodene.png"));
        Gui.drawModalRectWithCustomSizedTexture(x, y / 2 - textureheight / 2, 0, 0, texturewidth, textureheight, texturewidth, textureheight);
    }

    public void drawEridani(boolean Shadow, int x, int y, int texturewidth, int textureheight) {
        nowcanupdate1 = Minecraft.getMinecraft().Delay;
        String mode = new String();
        String shadowmode = new String();
        switch (style) {
            case 0 -> {
                mode = "outtouch/Eridani.png";
                shadowmode = "outtouch/EridaniS.png";
            }
            case 1 -> {
                mode = "outtouch/Eridani2.png";
                shadowmode = "outtouch/EridaniS2.png";
            }
            case 2 -> {
                mode = "outtouch/Eridani3.png";
                shadowmode = "outtouch/EridaniS3.png";
            }
        }
        if (Shadow) {
            mc.getTextureManager().bindTexture(new ResourceLocation(shadowmode));
            if (i6 > -4 && nowcanupdate1 > lastcanupdate1) {
                i6--;
                lastcanupdate1 = nowcanupdate1;
            } else if (i6 < -4) {
                i6++;
            }
            Gui.drawModalRectWithCustomSizedTexture(x + i6, y / 2 - textureheight / 2, 0, 0, texturewidth, textureheight, texturewidth, textureheight);
        }
        mc.getTextureManager().bindTexture(new ResourceLocation(mode));
        Gui.drawModalRectWithCustomSizedTexture(x, y / 2 - textureheight / 2, 0, 0, texturewidth, textureheight, texturewidth, textureheight);
    }

}
