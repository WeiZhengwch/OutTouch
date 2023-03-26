package net.optifine.shaders;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class HFNoiseTexture implements ICustomTexture {
    private final int textureUnit = 15;
    private int texID = GL11.glGenTextures();

    public HFNoiseTexture(int width, int height) {
        byte[] abyte = genHFNoiseImage(width, height);
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(abyte.length);
        bytebuffer.put(abyte);
        bytebuffer.flip();
        GlStateManager.bindTexture(texID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, bytebuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.bindTexture(0);
    }

    public int getID() {
        return texID;
    }

    public void deleteTexture() {
        GlStateManager.deleteTexture(texID);
        texID = 0;
    }

    private int random(int seed) {
        seed = seed ^ seed << 13;
        seed = seed ^ seed >> 17;
        seed = seed ^ seed << 5;
        return seed;
    }

    private byte random(int x, int y, int z) {
        int i = (random(x) + random(y * 19)) * random(z * 23) - z;
        return (byte) (random(i) % 128);
    }

    private byte[] genHFNoiseImage(int width, int height) {
        byte[] abyte = new byte[width * height * 3];
        int i = 0;

        for (int j = 0; j < height; ++j) {
            for (int k = 0; k < width; ++k) {
                for (int l = 1; l < 4; ++l) {
                    abyte[i++] = random(k, j, l);
                }
            }
        }

        return abyte;
    }

    public int getTextureId() {
        return texID;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public int getTarget() {
        return 3553;
    }
}
