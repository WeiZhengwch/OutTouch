package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TileEntitySignRenderer extends TileEntitySpecialRenderer<TileEntitySign> {
    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");
    private static double textRenderDistanceSq = 4096.0D;
    /**
     * The ModelSign instance for use in this renderer
     */
    private final ModelSign model = new ModelSign();

    private static boolean isRenderText(TileEntitySign p_isRenderText_0_) {
        if (Shaders.isShadowPass) {
            return false;
        } else if (Config.getMinecraft().currentScreen instanceof GuiEditSign) {
            return true;
        } else {
            if (!Config.zoomMode && p_isRenderText_0_.lineBeingEdited < 0) {
                Entity entity = Config.getMinecraft().getRenderViewEntity();
                double d0 = p_isRenderText_0_.getDistanceSq(entity.posX, entity.posY, entity.posZ);

                return !(d0 > textRenderDistanceSq);
            }

            return true;
        }
    }

    public static void updateTextRenderDistance() {
        Minecraft minecraft = Config.getMinecraft();
        double d0 = Config.limit(minecraft.gameSettings.fovSetting, 1.0F, 120.0F);
        double d1 = Math.max(1.5D * (double) minecraft.displayHeight / d0, 16.0D);
        textRenderDistanceSq = d1 * d1;
    }

    public void renderTileEntityAt(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage) {
        Block block = te.getBlockType();
        GlStateManager.pushMatrix();
        float f = 0.6666667F;

        if (block == Blocks.standing_sign) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F * f, (float) z + 0.5F);
            float f1 = (float) (te.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(-f1, 0.0F, 1.0F, 0.0F);
            model.signStick.showModel = true;
        } else {
            int k = te.getBlockMetadata();
            float f2 = 0.0F;

            if (k == 2) {
                f2 = 180.0F;
            }

            if (k == 4) {
                f2 = 90.0F;
            }

            if (k == 5) {
                f2 = -90.0F;
            }

            GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F * f, (float) z + 0.5F);
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            model.signStick.showModel = false;
        }

        if (destroyStage >= 0) {
            bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            bindTexture(SIGN_TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(f, -f, -f);
        model.renderSign();
        GlStateManager.popMatrix();

        if (isRenderText(te)) {
            FontRenderer fontrenderer = getFontRenderer();
            float f3 = 0.015625F * f;
            GlStateManager.translate(0.0F, 0.5F * f, 0.07F * f);
            GlStateManager.scale(f3, -f3, f3);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
            GlStateManager.depthMask(false);
            int i = 0;

            if (Config.isCustomColors()) {
                i = CustomColors.getSignTextColor(i);
            }

            if (destroyStage < 0) {
                for (int j = 0; j < te.signText.length; ++j) {
                    if (te.signText[j] != null) {
                        IChatComponent ichatcomponent = te.signText[j];
                        List<IChatComponent> list = GuiUtilRenderComponents.splitText(ichatcomponent, 90, fontrenderer, false, true);
                        String s = list != null && list.size() > 0 ? list.get(0).getFormattedText() : "";

                        if (j == te.lineBeingEdited) {
                            s = "> " + s + " <";
                            fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, i);
                        } else {
                            fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, i);
                        }
                    }
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
