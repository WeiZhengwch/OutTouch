package net.optifine.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelSprite {
    private final ModelRenderer modelRenderer;
    private final int textureOffsetX;
    private final int textureOffsetY;
    private final float posX;
    private final float posY;
    private final float posZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final float sizeAdd;
    private final float minU;
    private final float minV;
    private final float maxU;
    private final float maxV;

    public ModelSprite(ModelRenderer modelRenderer, int textureOffsetX, int textureOffsetY, float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd) {
        this.modelRenderer = modelRenderer;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeAdd = sizeAdd;
        minU = (float) textureOffsetX / modelRenderer.textureWidth;
        minV = (float) textureOffsetY / modelRenderer.textureHeight;
        maxU = (float) (textureOffsetX + sizeX) / modelRenderer.textureWidth;
        maxV = (float) (textureOffsetY + sizeY) / modelRenderer.textureHeight;
    }

    public static void renderItemIn2D(Tessellator tess, float minU, float minV, float maxU, float maxV, int sizeX, int sizeY, float width, float texWidth, float texHeight) {
        if (width < 6.25E-4F) {
            width = 6.25E-4F;
        }

        float f = maxU - minU;
        float f1 = maxV - minV;
        double d0 = MathHelper.abs(f) * (texWidth / 16.0F);
        double d1 = MathHelper.abs(f1) * (texHeight / 16.0F);
        WorldRenderer worldrenderer = tess.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, d1, 0.0D).tex(minU, maxV).endVertex();
        worldrenderer.pos(d0, d1, 0.0D).tex(maxU, maxV).endVertex();
        worldrenderer.pos(d0, 0.0D, 0.0D).tex(maxU, minV).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(minU, minV).endVertex();
        tess.draw();
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, 0.0D, width).tex(minU, minV).endVertex();
        worldrenderer.pos(d0, 0.0D, width).tex(maxU, minV).endVertex();
        worldrenderer.pos(d0, d1, width).tex(maxU, maxV).endVertex();
        worldrenderer.pos(0.0D, d1, width).tex(minU, maxV).endVertex();
        tess.draw();
        float f2 = 0.5F * f / (float) sizeX;
        float f3 = 0.5F * f1 / (float) sizeY;
        GL11.glNormal3f(-1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0; i < sizeX; ++i) {
            float f4 = (float) i / (float) sizeX;
            float f5 = minU + f * f4 + f2;
            worldrenderer.pos((double) f4 * d0, d1, width).tex(f5, maxV).endVertex();
            worldrenderer.pos((double) f4 * d0, d1, 0.0D).tex(f5, maxV).endVertex();
            worldrenderer.pos((double) f4 * d0, 0.0D, 0.0D).tex(f5, minV).endVertex();
            worldrenderer.pos((double) f4 * d0, 0.0D, width).tex(f5, minV).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int j = 0; j < sizeX; ++j) {
            float f7 = (float) j / (float) sizeX;
            float f10 = minU + f * f7 + f2;
            float f6 = f7 + 1.0F / (float) sizeX;
            worldrenderer.pos((double) f6 * d0, 0.0D, width).tex(f10, minV).endVertex();
            worldrenderer.pos((double) f6 * d0, 0.0D, 0.0D).tex(f10, minV).endVertex();
            worldrenderer.pos((double) f6 * d0, d1, 0.0D).tex(f10, maxV).endVertex();
            worldrenderer.pos((double) f6 * d0, d1, width).tex(f10, maxV).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int k = 0; k < sizeY; ++k) {
            float f8 = (float) k / (float) sizeY;
            float f11 = minV + f1 * f8 + f3;
            float f13 = f8 + 1.0F / (float) sizeY;
            worldrenderer.pos(0.0D, (double) f13 * d1, width).tex(minU, f11).endVertex();
            worldrenderer.pos(d0, (double) f13 * d1, width).tex(maxU, f11).endVertex();
            worldrenderer.pos(d0, (double) f13 * d1, 0.0D).tex(maxU, f11).endVertex();
            worldrenderer.pos(0.0D, (double) f13 * d1, 0.0D).tex(minU, f11).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int l = 0; l < sizeY; ++l) {
            float f9 = (float) l / (float) sizeY;
            float f12 = minV + f1 * f9 + f3;
            worldrenderer.pos(d0, (double) f9 * d1, width).tex(maxU, f12).endVertex();
            worldrenderer.pos(0.0D, (double) f9 * d1, width).tex(minU, f12).endVertex();
            worldrenderer.pos(0.0D, (double) f9 * d1, 0.0D).tex(minU, f12).endVertex();
            worldrenderer.pos(d0, (double) f9 * d1, 0.0D).tex(maxU, f12).endVertex();
        }

        tess.draw();
    }

    public void render(Tessellator tessellator, float scale) {
        GlStateManager.translate(posX * scale, posY * scale, posZ * scale);
        float f = minU;
        float f1 = maxU;
        float f2 = minV;
        float f3 = maxV;

        if (modelRenderer.mirror) {
            f = maxU;
            f1 = minU;
        }

        if (modelRenderer.mirrorV) {
            f2 = maxV;
            f3 = minV;
        }

        renderItemIn2D(tessellator, f, f2, f1, f3, sizeX, sizeY, scale * (float) sizeZ, modelRenderer.textureWidth, modelRenderer.textureHeight);
        GlStateManager.translate(-posX * scale, -posY * scale, -posZ * scale);
    }
}
