package net.minecraft.client.renderer;

import net.optifine.SmartAnimations;

public class Tessellator {
    /**
     * The static instance of the Tessellator.
     */
    private static final Tessellator instance = new Tessellator(2097152);
    private final WorldRenderer worldRenderer;
    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    public Tessellator(int bufferSize) {
        worldRenderer = new WorldRenderer(bufferSize);
    }

    public static Tessellator getInstance() {
        return instance;
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw() {
        if (worldRenderer.animatedSprites != null) {
            SmartAnimations.spritesRendered(worldRenderer.animatedSprites);
        }

        worldRenderer.finishDrawing();
        vboUploader.draw(worldRenderer);
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }
}
