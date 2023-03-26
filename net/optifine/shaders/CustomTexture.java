package net.optifine.shaders;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;

public class CustomTexture implements ICustomTexture {
    private int textureUnit = -1;
    private final String path;
    private final ITextureObject texture;

    public CustomTexture(int textureUnit, String path, ITextureObject texture) {
        this.textureUnit = textureUnit;
        this.path = path;
        this.texture = texture;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public String getPath() {
        return path;
    }

    public ITextureObject getTexture() {
        return texture;
    }

    public int getTextureId() {
        return texture.getGlTextureId();
    }

    public void deleteTexture() {
        TextureUtil.deleteTexture(texture.getGlTextureId());
    }

    public int getTarget() {
        return 3553;
    }

    public String toString() {
        return "textureUnit: " + textureUnit + ", path: " + path + ", glTextureId: " + getTextureId();
    }
}
