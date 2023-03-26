package net.optifine.shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.optifine.shaders.config.ShaderOption;

public class GuiSliderShaderOption extends GuiButtonShaderOption {
    public boolean dragging;
    private float sliderValue = 1.0F;
    private final ShaderOption shaderOption;

    public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text) {
        super(buttonId, x, y, w, h, shaderOption, text);
        this.shaderOption = shaderOption;
        sliderValue = shaderOption.getIndexNormalized();
        displayString = GuiShaderOptions.getButtonText(shaderOption, width);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            if (dragging && !GuiScreen.isShiftKeyDown()) {
                sliderValue = (float) (mouseX - (xPosition + 4)) / (float) (width - 8);
                sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
                shaderOption.setIndexNormalized(sliderValue);
                sliderValue = shaderOption.getIndexNormalized();
                displayString = GuiShaderOptions.getButtonText(shaderOption, width);
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)), yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)) + 4, yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            sliderValue = (float) (mouseX - (xPosition + 4)) / (float) (width - 8);
            sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
            shaderOption.setIndexNormalized(sliderValue);
            displayString = GuiShaderOptions.getButtonText(shaderOption, width);
            dragging = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }

    public void valueChanged() {
        sliderValue = shaderOption.getIndexNormalized();
    }

    public boolean isSwitchable() {
        return false;
    }
}
