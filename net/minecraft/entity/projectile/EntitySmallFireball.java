package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySmallFireball extends EntityFireball {
    public EntitySmallFireball(World worldIn) {
        super(worldIn);
        setSize(0.3125F, 0.3125F);
    }

    public EntitySmallFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
        super(worldIn, shooter, accelX, accelY, accelZ);
        setSize(0.3125F, 0.3125F);
    }

    public EntitySmallFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        super(worldIn, x, y, z, accelX, accelY, accelZ);
        setSize(0.3125F, 0.3125F);
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition movingObject) {
        if (!worldObj.isRemote) {
            if (movingObject.entityHit != null) {
                boolean flag = movingObject.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), 5.0F);

                if (flag) {
                    applyEnchantments(shootingEntity, movingObject.entityHit);

                    if (!movingObject.entityHit.isImmuneToFire()) {
                        movingObject.entityHit.setFire(5);
                    }
                }
            } else {
                boolean flag1 = true;

                if (shootingEntity != null && shootingEntity instanceof EntityLiving) {
                    flag1 = worldObj.getGameRules().getBoolean("mobGriefing");
                }

                if (flag1) {
                    BlockPos blockpos = movingObject.getBlockPos().offset(movingObject.sideHit);

                    if (worldObj.isAirBlock(blockpos)) {
                        worldObj.setBlockState(blockpos, Blocks.fire.getDefaultState());
                    }
                }
            }

            setDead();
        }
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }
}
