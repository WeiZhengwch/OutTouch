package net.optifine.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerItemModel {
    public static final int ATTACH_BODY = 0;
    public static final int ATTACH_HEAD = 1;
    public static final int ATTACH_LEFT_ARM = 2;
    public static final int ATTACH_RIGHT_ARM = 3;
    public static final int ATTACH_LEFT_LEG = 4;
    public static final int ATTACH_RIGHT_LEG = 5;
    public static final int ATTACH_CAPE = 6;
    private final ResourceLocation locationMissing = new ResourceLocation("textures/blocks/wool_colored_red.png");
    private final Dimension textureSize;
    private final boolean usePlayerTexture;
    private PlayerItemRenderer[] modelRenderers = new PlayerItemRenderer[0];
    private ResourceLocation textureLocation;
    private BufferedImage textureImage;
    private DynamicTexture texture;

    public PlayerItemModel(Dimension textureSize, boolean usePlayerTexture, PlayerItemRenderer[] modelRenderers) {
        this.textureSize = textureSize;
        this.usePlayerTexture = usePlayerTexture;
        this.modelRenderers = modelRenderers;
    }

    public static ModelRenderer getAttachModel(ModelBiped modelBiped, int attachTo) {
        return switch (attachTo) {
            case 0 -> modelBiped.bipedBody;
            case 1 -> modelBiped.bipedHead;
            case 2 -> modelBiped.bipedLeftArm;
            case 3 -> modelBiped.bipedRightArm;
            case 4 -> modelBiped.bipedLeftLeg;
            case 5 -> modelBiped.bipedRightLeg;
            default -> null;
        };
    }

    public void render(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks) {
        TextureManager texturemanager = Config.getTextureManager();

        if (usePlayerTexture) {
            texturemanager.bindTexture(player.getLocationSkin());
        } else if (textureLocation != null) {
            if (texture == null && textureImage != null) {
                texture = new DynamicTexture(textureImage);
                Minecraft.getMinecraft().getTextureManager().loadTexture(textureLocation, texture);
            }

            texturemanager.bindTexture(textureLocation);
        } else {
            texturemanager.bindTexture(locationMissing);
        }

        for (PlayerItemRenderer playeritemrenderer : modelRenderers) {
            GlStateManager.pushMatrix();

            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            playeritemrenderer.render(modelBiped, scale);
            GlStateManager.popMatrix();
        }
    }

    public BufferedImage getTextureImage() {
        return textureImage;
    }

    public void setTextureImage(BufferedImage textureImage) {
        this.textureImage = textureImage;
    }

    public DynamicTexture getTexture() {
        return texture;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    public boolean isUsePlayerTexture() {
        return usePlayerTexture;
    }
}
