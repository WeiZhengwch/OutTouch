package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Calendar;

public class EntityBat extends EntityAmbientCreature {
    /**
     * Coordinates of where the bat spawned.
     */
    private BlockPos spawnPosition;

    public EntityBat(World worldIn) {
        super(worldIn);
        setSize(0.5F, 0.9F);
        setIsBatHanging(true);
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, (byte) 0);
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume() {
        return 0.1F;
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    protected float getSoundPitch() {
        return super.getSoundPitch() * 0.95F;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound() {
        return getIsBatHanging() && rand.nextInt(4) != 0 ? null : "mob.bat.idle";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound() {
        return "mob.bat.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound() {
        return "mob.bat.death";
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed() {
        return false;
    }

    protected void collideWithEntity(Entity entityIn) {
    }

    protected void collideWithNearbyEntities() {
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D);
    }

    public boolean getIsBatHanging() {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setIsBatHanging(boolean isHanging) {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (isHanging) {
            dataWatcher.updateObject(16, (byte) (b0 | 1));
        } else {
            dataWatcher.updateObject(16, (byte) (b0 & -2));
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        super.onUpdate();

        if (getIsBatHanging()) {
            motionX = motionY = motionZ = 0.0D;
            posY = (double) MathHelper.floor_double(posY) + 1.0D - (double) height;
        } else {
            motionY *= 0.6000000238418579D;
        }
    }

    protected void updateAITasks() {
        super.updateAITasks();
        BlockPos blockpos = new BlockPos(this);
        BlockPos blockpos1 = blockpos.up();

        if (getIsBatHanging()) {
            if (!worldObj.getBlockState(blockpos1).getBlock().isNormalCube()) {
                setIsBatHanging(false);
                worldObj.playAuxSFXAtEntity(null, 1015, blockpos, 0);
            } else {
                if (rand.nextInt(200) == 0) {
                    rotationYawHead = (float) rand.nextInt(360);
                }

                if (worldObj.getClosestPlayerToEntity(this, 4.0D) != null) {
                    setIsBatHanging(false);
                    worldObj.playAuxSFXAtEntity(null, 1015, blockpos, 0);
                }
            }
        } else {
            if (spawnPosition != null && (!worldObj.isAirBlock(spawnPosition) || spawnPosition.getY() < 1)) {
                spawnPosition = null;
            }

            if (spawnPosition == null || rand.nextInt(30) == 0 || spawnPosition.distanceSq((int) posX, (int) posY, (int) posZ) < 4.0D) {
                spawnPosition = new BlockPos((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
            }

            double d0 = (double) spawnPosition.getX() + 0.5D - posX;
            double d1 = (double) spawnPosition.getY() + 0.1D - posY;
            double d2 = (double) spawnPosition.getZ() + 0.5D - posZ;
            motionX += (Math.signum(d0) * 0.5D - motionX) * 0.10000000149011612D;
            motionY += (Math.signum(d1) * 0.699999988079071D - motionY) * 0.10000000149011612D;
            motionZ += (Math.signum(d2) * 0.5D - motionZ) * 0.10000000149011612D;
            float f = (float) (MathHelper.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
            float f1 = MathHelper.wrapAngleTo180_float(f - rotationYaw);
            moveForward = 0.5F;
            rotationYaw += f1;

            if (rand.nextInt(100) == 0 && worldObj.getBlockState(blockpos1).getBlock().isNormalCube()) {
                setIsBatHanging(true);
            }
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking() {
        return false;
    }

    public void fall(float distance, float damageMultiplier) {
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
    }

    /**
     * Return whether this entity should NOT trigger a pressure plate or a tripwire.
     */
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            if (!worldObj.isRemote && getIsBatHanging()) {
                setIsBatHanging(false);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        dataWatcher.updateObject(16, tagCompund.getByte("BatFlags"));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setByte("BatFlags", dataWatcher.getWatchableObjectByte(16));
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere() {
        BlockPos blockpos = new BlockPos(posX, getEntityBoundingBox().minY, posZ);

        if (blockpos.getY() >= worldObj.getSeaLevel()) {
            return false;
        } else {
            int i = worldObj.getLightFromNeighbors(blockpos);
            int j = 4;

            if (isDateAroundHalloween(worldObj.getCurrentDate())) {
                j = 7;
            } else if (rand.nextBoolean()) {
                return false;
            }

            return i <= rand.nextInt(j) && super.getCanSpawnHere();
        }
    }

    private boolean isDateAroundHalloween(Calendar p_175569_1_) {
        return p_175569_1_.get(2) + 1 == 10 && p_175569_1_.get(5) >= 20 || p_175569_1_.get(2) + 1 == 11 && p_175569_1_.get(5) <= 3;
    }

    public float getEyeHeight() {
        return height / 2.0F;
    }
}
