package net.optifine.entity.model;

import net.minecraft.util.ResourceLocation;

public class CustomEntityRenderer {
    private final String name;
    private final String basePath;
    private final ResourceLocation textureLocation;
    private final CustomModelRenderer[] customModelRenderers;
    private final float shadowSize;

    public CustomEntityRenderer(String name, String basePath, ResourceLocation textureLocation, CustomModelRenderer[] customModelRenderers, float shadowSize) {
        this.name = name;
        this.basePath = basePath;
        this.textureLocation = textureLocation;
        this.customModelRenderers = customModelRenderers;
        this.shadowSize = shadowSize;
    }

    public String getName() {
        return name;
    }

    public String getBasePath() {
        return basePath;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public CustomModelRenderer[] getCustomModelRenderers() {
        return customModelRenderers;
    }

    public float getShadowSize() {
        return shadowSize;
    }
}
