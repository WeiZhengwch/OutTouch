package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class Barrier extends EntityFX {
    protected Barrier(World worldIn, double p_i46286_2_, double p_i46286_4_, double p_i46286_6_, Item p_i46286_8_) {
        super(worldIn, p_i46286_2_, p_i46286_4_, p_i46286_6_, 0.0D, 0.0D, 0.0D);
        setParticleIcon(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(p_i46286_8_));
        particleRed = particleGreen = particleBlue = 1.0F;
        motionX = motionY = motionZ = 0.0D;
        particleGravity = 0.0F;
        particleMaxAge = 80;
    }

    public int getFXLayer() {
        return 1;
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = particleIcon.getMinU();
        float f1 = particleIcon.getMaxU();
        float f2 = particleIcon.getMinV();
        float f3 = particleIcon.getMaxV();
        float f4 = 0.5F;
        float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
        int i = getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        worldRendererIn.pos(f5 - rotationX * 0.5F - rotationXY * 0.5F, f6 - rotationZ * 0.5F, f7 - rotationYZ * 0.5F - rotationXZ * 0.5F).tex(f1, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 - rotationX * 0.5F + rotationXY * 0.5F, f6 + rotationZ * 0.5F, f7 - rotationYZ * 0.5F + rotationXZ * 0.5F).tex(f1, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * 0.5F + rotationXY * 0.5F, f6 + rotationZ * 0.5F, f7 + rotationYZ * 0.5F + rotationXZ * 0.5F).tex(f, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * 0.5F - rotationXY * 0.5F, f6 - rotationZ * 0.5F, f7 + rotationYZ * 0.5F - rotationXZ * 0.5F).tex(f, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return new Barrier(worldIn, xCoordIn, yCoordIn, zCoordIn, Item.getItemFromBlock(Blocks.barrier));
        }
    }
}
