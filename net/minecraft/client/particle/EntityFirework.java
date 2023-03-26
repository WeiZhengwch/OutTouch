package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFirework {
    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            EntityFirework.SparkFX entityfirework$sparkfx = new EntityFirework.SparkFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Minecraft.getMinecraft().effectRenderer);
            entityfirework$sparkfx.setAlphaF(0.99F);
            return entityfirework$sparkfx;
        }
    }

    public static class OverlayFX extends EntityFX {
        protected OverlayFX(World p_i46466_1_, double p_i46466_2_, double p_i46466_4_, double p_i46466_6_) {
            super(p_i46466_1_, p_i46466_2_, p_i46466_4_, p_i46466_6_);
            particleMaxAge = 4;
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
            float f = 0.25F;
            float f1 = 0.5F;
            float f2 = 0.125F;
            float f3 = 0.375F;
            float f4 = 7.1F * MathHelper.sin(((float) particleAge + partialTicks - 1.0F) * 0.25F * (float) Math.PI);
            particleAlpha = 0.6F - ((float) particleAge + partialTicks - 1.0F) * 0.25F * 0.5F;
            float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
            float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
            float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);
            int i = getBrightnessForRender(partialTicks);
            int j = i >> 16 & 65535;
            int k = i & 65535;
            worldRendererIn.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(0.5D, 0.375D).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
            worldRendererIn.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(0.5D, 0.125D).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
            worldRendererIn.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(0.25D, 0.125D).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
            worldRendererIn.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(0.25D, 0.375D).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(j, k).endVertex();
        }
    }

    public static class SparkFX extends EntityFX {
        private final EffectRenderer field_92047_az;
        private boolean trail;
        private boolean twinkle;
        private float fadeColourRed;
        private float fadeColourGreen;
        private float fadeColourBlue;
        private boolean hasFadeColour;

        public SparkFX(World p_i46465_1_, double p_i46465_2_, double p_i46465_4_, double p_i46465_6_, double p_i46465_8_, double p_i46465_10_, double p_i46465_12_, EffectRenderer p_i46465_14_) {
            super(p_i46465_1_, p_i46465_2_, p_i46465_4_, p_i46465_6_);
            motionX = p_i46465_8_;
            motionY = p_i46465_10_;
            motionZ = p_i46465_12_;
            field_92047_az = p_i46465_14_;
            particleScale *= 0.75F;
            particleMaxAge = 48 + rand.nextInt(12);
            noClip = false;
        }

        public void setTrail(boolean trailIn) {
            trail = trailIn;
        }

        public void setTwinkle(boolean twinkleIn) {
            twinkle = twinkleIn;
        }

        public void setColour(int colour) {
            float f = (float) ((colour & 16711680) >> 16) / 255.0F;
            float f1 = (float) ((colour & 65280) >> 8) / 255.0F;
            float f2 = (float) ((colour & 255)) / 255.0F;
            float f3 = 1.0F;
            setRBGColorF(f * f3, f1 * f3, f2 * f3);
        }

        public void setFadeColour(int faceColour) {
            fadeColourRed = (float) ((faceColour & 16711680) >> 16) / 255.0F;
            fadeColourGreen = (float) ((faceColour & 65280) >> 8) / 255.0F;
            fadeColourBlue = (float) ((faceColour & 255)) / 255.0F;
            hasFadeColour = true;
        }

        public AxisAlignedBB getCollisionBoundingBox() {
            return null;
        }

        public boolean canBePushed() {
            return false;
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
            if (!twinkle || particleAge < particleMaxAge / 3 || (particleAge + particleMaxAge) / 3 % 2 == 0) {
                super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
        }

        public void onUpdate() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            if (particleAge++ >= particleMaxAge) {
                setDead();
            }

            if (particleAge > particleMaxAge / 2) {
                setAlphaF(1.0F - ((float) particleAge - (float) (particleMaxAge / 2)) / (float) particleMaxAge);

                if (hasFadeColour) {
                    particleRed += (fadeColourRed - particleRed) * 0.2F;
                    particleGreen += (fadeColourGreen - particleGreen) * 0.2F;
                    particleBlue += (fadeColourBlue - particleBlue) * 0.2F;
                }
            }

            int baseTextureIndex = 160;
            setParticleTextureIndex(baseTextureIndex + (7 - particleAge * 8 / particleMaxAge));
            motionY -= 0.004D;
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.9100000262260437D;
            motionY *= 0.9100000262260437D;
            motionZ *= 0.9100000262260437D;

            if (onGround) {
                motionX *= 0.699999988079071D;
                motionZ *= 0.699999988079071D;
            }

            if (trail && particleAge < particleMaxAge / 2 && (particleAge + particleMaxAge) % 2 == 0) {
                EntityFirework.SparkFX entityfirework$sparkfx = new EntityFirework.SparkFX(worldObj, posX, posY, posZ, 0.0D, 0.0D, 0.0D, field_92047_az);
                entityfirework$sparkfx.setAlphaF(0.99F);
                entityfirework$sparkfx.setRBGColorF(particleRed, particleGreen, particleBlue);
                entityfirework$sparkfx.particleAge = entityfirework$sparkfx.particleMaxAge / 2;

                if (hasFadeColour) {
                    entityfirework$sparkfx.hasFadeColour = true;
                    entityfirework$sparkfx.fadeColourRed = fadeColourRed;
                    entityfirework$sparkfx.fadeColourGreen = fadeColourGreen;
                    entityfirework$sparkfx.fadeColourBlue = fadeColourBlue;
                }

                entityfirework$sparkfx.twinkle = twinkle;
                field_92047_az.addEffect(entityfirework$sparkfx);
            }
        }

        public int getBrightnessForRender(float partialTicks) {
            return 15728880;
        }

        public float getBrightness(float partialTicks) {
            return 1.0F;
        }
    }

    public static class StarterFX extends EntityFX {
        private final EffectRenderer theEffectRenderer;
        boolean twinkle;
        private int fireworkAge;
        private NBTTagList fireworkExplosions;

        public StarterFX(World p_i46464_1_, double p_i46464_2_, double p_i46464_4_, double p_i46464_6_, double p_i46464_8_, double p_i46464_10_, double p_i46464_12_, EffectRenderer p_i46464_14_, NBTTagCompound p_i46464_15_) {
            super(p_i46464_1_, p_i46464_2_, p_i46464_4_, p_i46464_6_, 0.0D, 0.0D, 0.0D);
            motionX = p_i46464_8_;
            motionY = p_i46464_10_;
            motionZ = p_i46464_12_;
            theEffectRenderer = p_i46464_14_;
            particleMaxAge = 8;

            if (p_i46464_15_ != null) {
                fireworkExplosions = p_i46464_15_.getTagList("Explosions", 10);

                if (fireworkExplosions.tagCount() == 0) {
                    fireworkExplosions = null;
                } else {
                    particleMaxAge = fireworkExplosions.tagCount() * 2 - 1;

                    for (int i = 0; i < fireworkExplosions.tagCount(); ++i) {
                        NBTTagCompound nbttagcompound = fireworkExplosions.getCompoundTagAt(i);

                        if (nbttagcompound.getBoolean("Flicker")) {
                            twinkle = true;
                            particleMaxAge += 15;
                            break;
                        }
                    }
                }
            }
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        }

        public void onUpdate() {
            if (fireworkAge == 0 && fireworkExplosions != null) {
                boolean flag = func_92037_i();
                boolean flag1 = false;

                if (fireworkExplosions.tagCount() >= 3) {
                    flag1 = true;
                } else {
                    for (int i = 0; i < fireworkExplosions.tagCount(); ++i) {
                        NBTTagCompound nbttagcompound = fireworkExplosions.getCompoundTagAt(i);

                        if (nbttagcompound.getByte("Type") == 1) {
                            flag1 = true;
                            break;
                        }
                    }
                }

                String s1 = "fireworks." + (flag1 ? "largeBlast" : "blast") + (flag ? "_far" : "");
                worldObj.playSound(posX, posY, posZ, s1, 20.0F, 0.95F + rand.nextFloat() * 0.1F, true);
            }

            if (fireworkAge % 2 == 0 && fireworkExplosions != null && fireworkAge / 2 < fireworkExplosions.tagCount()) {
                int k = fireworkAge / 2;
                NBTTagCompound nbttagcompound1 = fireworkExplosions.getCompoundTagAt(k);
                int l = nbttagcompound1.getByte("Type");
                boolean flag4 = nbttagcompound1.getBoolean("Trail");
                boolean flag2 = nbttagcompound1.getBoolean("Flicker");
                int[] aint = nbttagcompound1.getIntArray("Colors");
                int[] aint1 = nbttagcompound1.getIntArray("FadeColors");

                if (aint.length == 0) {
                    aint = new int[]{ItemDye.dyeColors[0]};
                }

                switch (l) {
                    case 1 -> createBall(0.5D, 4, aint, aint1, flag4, flag2);
                    case 2 ->
                            createShaped(new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, aint, aint1, flag4, flag2, false);
                    case 3 ->
                            createShaped(new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, aint, aint1, flag4, flag2, true);
                    case 4 -> createBurst(aint, aint1, flag4, flag2);
                    default -> createBall(0.25D, 2, aint, aint1, flag4, flag2);
                }

                int j = aint[0];
                float f = (float) ((j & 16711680) >> 16) / 255.0F;
                float f1 = (float) ((j & 65280) >> 8) / 255.0F;
                float f2 = (float) ((j & 255)) / 255.0F;
                EntityFirework.OverlayFX entityfirework$overlayfx = new EntityFirework.OverlayFX(worldObj, posX, posY, posZ);
                entityfirework$overlayfx.setRBGColorF(f, f1, f2);
                theEffectRenderer.addEffect(entityfirework$overlayfx);
            }

            ++fireworkAge;

            if (fireworkAge > particleMaxAge) {
                if (twinkle) {
                    boolean flag3 = func_92037_i();
                    String s = "fireworks." + (flag3 ? "twinkle_far" : "twinkle");
                    worldObj.playSound(posX, posY, posZ, s, 20.0F, 0.9F + rand.nextFloat() * 0.15F, true);
                }

                setDead();
            }
        }

        private boolean func_92037_i() {
            Minecraft minecraft = Minecraft.getMinecraft();
            return minecraft == null || minecraft.getRenderViewEntity() == null || minecraft.getRenderViewEntity().getDistanceSq(posX, posY, posZ) >= 256.0D;
        }

        private void createParticle(double p_92034_1_, double p_92034_3_, double p_92034_5_, double p_92034_7_, double p_92034_9_, double p_92034_11_, int[] p_92034_13_, int[] p_92034_14_, boolean p_92034_15_, boolean p_92034_16_) {
            EntityFirework.SparkFX entityfirework$sparkfx = new EntityFirework.SparkFX(worldObj, p_92034_1_, p_92034_3_, p_92034_5_, p_92034_7_, p_92034_9_, p_92034_11_, theEffectRenderer);
            entityfirework$sparkfx.setAlphaF(0.99F);
            entityfirework$sparkfx.setTrail(p_92034_15_);
            entityfirework$sparkfx.setTwinkle(p_92034_16_);
            int i = rand.nextInt(p_92034_13_.length);
            entityfirework$sparkfx.setColour(p_92034_13_[i]);

            if (p_92034_14_ != null && p_92034_14_.length > 0) {
                entityfirework$sparkfx.setFadeColour(p_92034_14_[rand.nextInt(p_92034_14_.length)]);
            }

            theEffectRenderer.addEffect(entityfirework$sparkfx);
        }

        private void createBall(double speed, int size, int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn) {
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;

            for (int i = -size; i <= size; ++i) {
                for (int j = -size; j <= size; ++j) {
                    for (int k = -size; k <= size; ++k) {
                        double d3 = (double) j + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                        double d4 = (double) i + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                        double d5 = (double) k + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                        double d6 = (double) MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5) / speed + rand.nextGaussian() * 0.05D;
                        createParticle(d0, d1, d2, d3 / d6, d4 / d6, d5 / d6, colours, fadeColours, trail, twinkleIn);

                        if (i != -size && i != size && j != -size && j != size) {
                            k += size * 2 - 1;
                        }
                    }
                }
            }
        }

        private void createShaped(double[][] shape, int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn, boolean p_92038_8_) {
            double d0 = shape[0][0];
            double d1 = shape[0][1];
            createParticle(posX, posY, posZ, d0 * 0.5, d1 * 0.5, 0.0D, colours, fadeColours, trail, twinkleIn);
            float f = rand.nextFloat() * (float) Math.PI;
            double d2 = p_92038_8_ ? 0.034D : 0.34D;

            for (int i = 0; i < 3; ++i) {
                double d3 = (double) f + (double) ((float) i * (float) Math.PI) * d2;
                double d4 = d0;
                double d5 = d1;

                for (int j = 1; j < shape.length; ++j) {
                    double d6 = shape[j][0];
                    double d7 = shape[j][1];

                    for (double d8 = 0.25D; d8 <= 1.0D; d8 += 0.25D) {
                        double d9 = (d4 + (d6 - d4) * d8) * 0.5;
                        double d10 = (d5 + (d7 - d5) * d8) * 0.5;
                        double d11 = d9 * Math.sin(d3);
                        d9 = d9 * Math.cos(d3);

                        for (double d12 = -1.0D; d12 <= 1.0D; d12 += 2.0D) {
                            createParticle(posX, posY, posZ, d9 * d12, d10, d11 * d12, colours, fadeColours, trail, twinkleIn);
                        }
                    }

                    d4 = d6;
                    d5 = d7;
                }
            }
        }

        private void createBurst(int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn) {
            double d0 = rand.nextGaussian() * 0.05D;
            double d1 = rand.nextGaussian() * 0.05D;

            for (int i = 0; i < 70; ++i) {
                double d2 = motionX * 0.5D + rand.nextGaussian() * 0.15D + d0;
                double d3 = motionZ * 0.5D + rand.nextGaussian() * 0.15D + d1;
                double d4 = motionY * 0.5D + rand.nextDouble() * 0.5D;
                createParticle(posX, posY, posZ, d2, d4, d3, colours, fadeColours, trail, twinkleIn);
            }
        }

        public int getFXLayer() {
            return 0;
        }
    }
}
