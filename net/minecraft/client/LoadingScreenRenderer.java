package net.minecraft.client;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;

public class LoadingScreenRenderer implements IProgressUpdate {
    /**
     * A reference to the Minecraft object.
     */
    private final Minecraft mc;
    private final ScaledResolution scaledResolution;
    private final Framebuffer framebuffer;
    private final String message = "";
    /**
     * The text currently displayed (i.e. the argument to the last call to printText or displayString)
     */
    private String currentlyDisplayedText = "";
    /**
     * The system's time represented in milliseconds.
     */
    private final long systemTime = Minecraft.getSystemTime();
    /**
     * True if the loading ended with a success
     */
    private boolean loadingSuccess;

    public LoadingScreenRenderer(Minecraft mcIn) {
        mc = mcIn;
        scaledResolution = new ScaledResolution(mcIn);
        framebuffer = new Framebuffer(mcIn.displayWidth, mcIn.displayHeight, false);
        framebuffer.setFramebufferFilter(9728);
    }

    /**
     * this string, followed by "working..." and then the "% complete" are the 3 lines shown. This resets progress to 0,
     * and the WorkingString to "working...".
     */
    public void resetProgressAndMessage(String message) {
        loadingSuccess = false;
        displayString(message);
    }

    /**
     * Shows the 'Saving level' string.
     */
    public void displaySavingString(String message) {
        loadingSuccess = true;
        displayString(message);
    }

    private void displayString(String message) {
        currentlyDisplayedText = message;

        if (!mc.running) {
            if (!loadingSuccess) {
                throw new MinecraftError();
            }
//        } else {
//            GlStateManager.clear(256);
//            GlStateManager.matrixMode(5889);
//            GlStateManager.loadIdentity();
//
//            if (OpenGlHelper.isFramebufferEnabled()) {
//                int i = scaledResolution.getScaleFactor();
//                GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth() * i, scaledResolution.getScaledHeight() * i, 0.0D, 100.0D, 300.0D);
//            } else {
//                ScaledResolution scaledresolution = new ScaledResolution(mc);
//                GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
//            }
//
//            GlStateManager.matrixMode(5888);
//            GlStateManager.loadIdentity();
//            GlStateManager.translate(0.0F, 0.0F, -200.0F);
        }
    }

    /**
     * Displays a string on the loading screen supposed to indicate what is being done currently.
     */
    public void displayLoadingString(String message) {
        if (!mc.running) {
            if (!loadingSuccess) {
                throw new MinecraftError();
            }
        }
//        } else {
//            systemTime = 0L;
//            this.message = message;
//            setLoadingProgress(-1);
//            systemTime = 0L;
//        }
    }

    /**
     * Updates the progress bar on the loading screen to the specified amount. Args: loadProgress
     */
    public void setLoadingProgress(int progress) {
//        if (!mc.running) {
//            if (!loadingSuccess) {
//                throw new MinecraftError();
//            }
//        } else {
//            long i = Minecraft.getSystemTime();
//
//            if (i - systemTime >= 100L) {
//                systemTime = i;
//                ScaledResolution scaledresolution = new ScaledResolution(mc);
//                int j = scaledresolution.getScaleFactor();
//                int k = scaledresolution.getScaledWidth();
//                int l = scaledresolution.getScaledHeight();
//
//                if (OpenGlHelper.isFramebufferEnabled()) {
//                    framebuffer.framebufferClear();
//                } else {
//                    GlStateManager.clear(256);
//                }
//
//                framebuffer.bindFramebuffer(false);
//                GlStateManager.matrixMode(5889);
//                GlStateManager.loadIdentity();
//                GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
//                GlStateManager.matrixMode(5888);
//                GlStateManager.loadIdentity();
//                GlStateManager.translate(0.0F, 0.0F, -200.0F);
//
//                if (!OpenGlHelper.isFramebufferEnabled()) {
//                    GlStateManager.clear(16640);
//                }
//
//                boolean flag = true;
//
//                if (Reflector.FMLClientHandler_handleLoadingScreen.exists()) {
//                    Object object = Reflector.call(Reflector.FMLClientHandler_instance);
//
//                    if (object != null) {
//                        flag = !Reflector.callBoolean(object, Reflector.FMLClientHandler_handleLoadingScreen, scaledresolution);
//                    }
//                }
//
//                if (flag) {
//                    Tessellator tessellator = Tessellator.getInstance();
//                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
//                    CustomLoadingScreen customloadingscreen = CustomLoadingScreens.getCustomLoadingScreen();
//
//                    if (customloadingscreen != null) {
//                        customloadingscreen.drawBackground(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
//                    } else {
//                        mc.getTextureManager().bindTexture(Gui.optionsBackground);
//                        float f = 32.0F;
//                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//                        worldrenderer.pos(0.0D, l, 0.0D).tex(0.0D, (float) l / f).color(64, 64, 64, 255).endVertex();
//                        worldrenderer.pos(k, l, 0.0D).tex((float) k / f, (float) l / f).color(64, 64, 64, 255).endVertex();
//                        worldrenderer.pos(k, 0.0D, 0.0D).tex((float) k / f, 0.0D).color(64, 64, 64, 255).endVertex();
//                        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
//                        tessellator.draw();
//                    }
//
//                    if (progress >= 0) {
//                        int l1 = 100;
//                        int i1 = 2;
//                        int j1 = k / 2 - l1 / 2;
//                        int k1 = l / 2 + 16;
//                        GlStateManager.disableTexture2D();
//                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
//                        worldrenderer.pos(j1, k1, 0.0D).color(128, 128, 128, 255).endVertex();
//                        worldrenderer.pos(j1, k1 + i1, 0.0D).color(128, 128, 128, 255).endVertex();
//                        worldrenderer.pos(j1 + l1, k1 + i1, 0.0D).color(128, 128, 128, 255).endVertex();
//                        worldrenderer.pos(j1 + l1, k1, 0.0D).color(128, 128, 128, 255).endVertex();
//                        worldrenderer.pos(j1, k1, 0.0D).color(128, 255, 128, 255).endVertex();
//                        worldrenderer.pos(j1, k1 + i1, 0.0D).color(128, 255, 128, 255).endVertex();
//                        worldrenderer.pos(j1 + progress, k1 + i1, 0.0D).color(128, 255, 128, 255).endVertex();
//                        worldrenderer.pos(j1 + progress, k1, 0.0D).color(128, 255, 128, 255).endVertex();
//                        tessellator.draw();
//                        GlStateManager.enableTexture2D();
//                    }
//
//                    GlStateManager.enableBlend();
//                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//                    mc.fontRendererObj.drawStringWithShadow(currentlyDisplayedText, (float) ((k - mc.fontRendererObj.getStringWidth(currentlyDisplayedText)) / 2), (float) (l / 2 - 4 - 16), 16777215);
//                    mc.fontRendererObj.drawStringWithShadow(message, (float) ((k - mc.fontRendererObj.getStringWidth(message)) / 2), (float) (l / 2 - 4 + 8), 16777215);
//                }
//
//                framebuffer.unbindFramebuffer();
//
//                if (OpenGlHelper.isFramebufferEnabled()) {
//                    framebuffer.framebufferRender(k * j, l * j);
//                }
//
//                mc.updateDisplay();
//
//                try {
//                    Thread.yield();
//                } catch (Exception ignored) {
//                }
//            }
//        }
    }

    public void setDoneWorking() {
    }
}
