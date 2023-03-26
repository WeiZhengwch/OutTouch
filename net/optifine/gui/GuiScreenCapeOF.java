package net.optifine.gui;

import com.mojang.authlib.exceptions.InvalidCredentialsException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;
import net.optifine.Lang;

import java.math.BigInteger;
import java.net.URI;
import java.util.Random;

public class GuiScreenCapeOF extends GuiScreenOF {
    private final GuiScreen parentScreen;
    private final FontRenderer fontRenderer;
    private String title;
    private String message;
    private long messageHideTimeMs;
    private String linkUrl;
    private GuiButtonOF buttonCopyLink;

    public GuiScreenCapeOF(GuiScreen parentScreenIn) {
        fontRenderer = Config.getMinecraft().fontRendererObj;
        parentScreen = parentScreenIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        int i = 0;
        title = I18n.format("of.options.capeOF.title");
        i = i + 2;
        buttonList.add(new GuiButtonOF(210, width / 2 - 155, height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.openEditor")));
        buttonList.add(new GuiButtonOF(220, width / 2 - 155 + 160, height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.reloadCape")));
        i = i + 6;
        buttonCopyLink = new GuiButtonOF(230, width / 2 - 100, height / 6 + 24 * (i >> 1), 200, 20, I18n.format("of.options.capeOF.copyEditorLink"));
        buttonCopyLink.visible = linkUrl != null;
        buttonList.add(buttonCopyLink);
        i = i + 4;
        buttonList.add(new GuiButtonOF(200, width / 2 - 100, height / 6 + 24 * (i >> 1), I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 200) {
                mc.displayGuiScreen(parentScreen);
            }

            if (button.id == 210) {
                try {
                    String s = mc.getSession().getProfile().getName();
                    String s1 = mc.getSession().getProfile().getId().toString().replace("-", "");
                    String s2 = mc.getSession().getToken();
                    Random random = new Random();
                    Random random1 = new Random(System.identityHashCode(new Object()));
                    BigInteger biginteger = new BigInteger(128, random);
                    BigInteger biginteger1 = new BigInteger(128, random1);
                    BigInteger biginteger2 = biginteger.xor(biginteger1);
                    String s3 = biginteger2.toString(16);
                    mc.getSessionService().joinServer(mc.getSession().getProfile(), s2, s3);
                    String s4 = "https://optifine.net/capeChange?u=" + s1 + "&n=" + s + "&s=" + s3;
                    boolean flag = Config.openWebLink(new URI(s4));

                    if (flag) {
                        showMessage(Lang.get("of.message.capeOF.openEditor"), 10000L);
                    } else {
                        showMessage(Lang.get("of.message.capeOF.openEditorError"), 10000L);
                        setLinkUrl(s4);
                    }
                } catch (InvalidCredentialsException invalidcredentialsexception) {
                    Config.showGuiMessage(I18n.format("of.message.capeOF.error1"), I18n.format("of.message.capeOF.error2", invalidcredentialsexception.getMessage()));
                    Config.warn("Mojang authentication failed");
                    Config.warn(invalidcredentialsexception.getClass().getName() + ": " + invalidcredentialsexception.getMessage());
                } catch (Exception exception) {
                    Config.warn("Error opening OptiFine cape link");
                    Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
                }
            }

            if (button.id == 220) {
                showMessage(Lang.get("of.message.capeOF.reloadCape"), 15000L);

                if (mc.thePlayer != null) {
                    long i = 15000L;
                    long j = System.currentTimeMillis() + i;
                    mc.thePlayer.setReloadCapeTimeMs(j);
                }
            }

            if (button.id == 230 && linkUrl != null) {
                setClipboardString(linkUrl);
            }
        }
    }

    private void showMessage(String msg, long timeMs) {
        message = msg;
        messageHideTimeMs = System.currentTimeMillis() + timeMs;
        setLinkUrl(null);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc.theWorld == null) {
            drawDefaultBackground();
        }
        drawCenteredString(fontRenderer, title, width / 2, 20, 16777215);
        if (message != null) {
            drawCenteredString(fontRenderer, message, width / 2, height / 6 + 60, 16777215);

            if (System.currentTimeMillis() > messageHideTimeMs) {
                message = null;
                setLinkUrl(null);
            }
        }
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
        buttonCopyLink.visible = linkUrl != null;
    }
}
