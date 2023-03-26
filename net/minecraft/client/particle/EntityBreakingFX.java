package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityBreakingFX extends EntityFX {
    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, Item p_i1195_8_) {
        this(worldIn, posXIn, posYIn, posZIn, p_i1195_8_, 0);
    }

    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, Item p_i1197_14_, int p_i1197_15_) {
        this(worldIn, posXIn, posYIn, posZIn, p_i1197_14_, p_i1197_15_);
        motionX *= 0.10000000149011612D;
        motionY *= 0.10000000149011612D;
        motionZ *= 0.10000000149011612D;
        motionX += xSpeedIn;
        motionY += ySpeedIn;
        motionZ += zSpeedIn;
    }

    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, Item p_i1196_8_, int p_i1196_9_) {
        super(worldIn, posXIn, posYIn, posZIn, 0.0D, 0.0D, 0.0D);
        setParticleIcon(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(p_i1196_8_, p_i1196_9_));
        particleRed = particleGreen = particleBlue = 1.0F;
        particleGravity = Blocks.snow.blockParticleGravity;
        particleScale /= 2.0F;
    }

    public int getFXLayer() {
        return 1;
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = ((float) particleTextureIndexX + particleTextureJitterX / 4.0F) / 16.0F;
        float f1 = f + 0.015609375F;
        float f2 = ((float) particleTextureIndexY + particleTextureJitterY / 4.0F) / 16.0F;
        float f3 = f2 + 0.015609375F;
        float f4 = 0.1F * particleScale;

        if (particleIcon != null) {
            f = particleIcon.getInterpolatedU(particleTextureJitterX / 4.0F * 16.0F);
            f1 = particleIcon.getInterpolatedU((particleTextureJitterX + 1.0F) / 4.0F * 16.0F);
            f2 = particleIcon.getInterpolatedV(particleTextureJitterY / 4.0F * 16.0F);
            f3 = particleIcon.getInterpolatedV((particleTextureJitterY + 1.0F) / 4.0F * 16.0F);
        }

        float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
        int i = getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        worldRendererIn.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(f, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(f, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(f1, f2).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
        worldRendererIn.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(f1, f3).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(j, k).endVertex();
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            int i = p_178902_15_.length > 1 ? p_178902_15_[1] : 0;
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Item.getItemById(p_178902_15_[0]), i);
        }
    }

    public static class SlimeFactory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Items.slime_ball);
        }
    }

    public static class SnowballFactory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Items.snowball);
        }
    }
}
