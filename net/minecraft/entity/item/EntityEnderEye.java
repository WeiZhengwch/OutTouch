package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityEnderEye extends Entity {
    /**
     * 'x' location the eye should float towards.
     */
    private double targetX;

    /**
     * 'y' location the eye should float towards.
     */
    private double targetY;

    /**
     * 'z' location the eye should float towards.
     */
    private double targetZ;
    private int despawnTimer;
    private boolean shatterOrDrop;

    public EntityEnderEye(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    public EntityEnderEye(World worldIn, double x, double y, double z) {
        super(worldIn);
        despawnTimer = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
    }

    protected void entityInit() {
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void moveTowards(BlockPos p_180465_1_) {
        double d0 = p_180465_1_.getX();
        int i = p_180465_1_.getY();
        double d1 = p_180465_1_.getZ();
        double d2 = d0 - posX;
        double d3 = d1 - posZ;
        float f = MathHelper.sqrt_double(d2 * d2 + d3 * d3);

        if (f > 12.0F) {
            targetX = posX + d2 / (double) f * 12.0D;
            targetZ = posZ + d3 / (double) f * 12.0D;
            targetY = posY + 8.0D;
        } else {
            targetX = d0;
            targetY = i;
            targetZ = d1;
        }

        despawnTimer = 0;
        shatterOrDrop = rand.nextInt(5) > 0;
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt_double(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, f) * 180.0D / Math.PI);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * 180.0D / Math.PI);

        for (rotationPitch = (float) (MathHelper.atan2(motionY, f) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F) {
        }

        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }

        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        if (!worldObj.isRemote) {
            double d0 = targetX - posX;
            double d1 = targetZ - posZ;
            float f1 = (float) Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float) MathHelper.atan2(d1, d0);
            double d2 = (double) f + (double) (f1 - f) * 0.0025D;

            if (f1 < 1.0F) {
                d2 *= 0.8D;
                motionY *= 0.8D;
            }

            motionX = Math.cos(f2) * d2;
            motionZ = Math.sin(f2) * d2;

            if (posY < targetY) {
                motionY += (1.0D - motionY) * 0.014999999664723873D;
            } else {
                motionY += (-1.0D - motionY) * 0.014999999664723873D;
            }
        }

        float f3 = 0.25F;

        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double) f3, posY - motionY * (double) f3, posZ - motionZ * (double) f3, motionX, motionY, motionZ);
            }
        } else {
            worldObj.spawnParticle(EnumParticleTypes.PORTAL, posX - motionX * (double) f3 + rand.nextDouble() * 0.6D - 0.3D, posY - motionY * (double) f3 - 0.5D, posZ - motionZ * (double) f3 + rand.nextDouble() * 0.6D - 0.3D, motionX, motionY, motionZ);
        }

        if (!worldObj.isRemote) {
            setPosition(posX, posY, posZ);
            ++despawnTimer;

            if (despawnTimer > 80 && !worldObj.isRemote) {
                setDead();

                if (shatterOrDrop) {
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, new ItemStack(Items.ender_eye)));
                } else {
                    worldObj.playAuxSFX(2003, new BlockPos(this), 0);
                }
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks) {
        return 1.0F;
    }

    public int getBrightnessForRender(float partialTicks) {
        return 15728880;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem() {
        return false;
    }
}
