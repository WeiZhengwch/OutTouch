package net.minecraft.entity.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityExpBottle extends EntityThrowable {
    public EntityExpBottle(World worldIn) {
        super(worldIn);
    }

    public EntityExpBottle(World worldIn, EntityLivingBase p_i1786_2_) {
        super(worldIn, p_i1786_2_);
    }

    public EntityExpBottle(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    protected float getGravityVelocity() {
        return 0.07F;
    }

    protected float getVelocity() {
        return 0.7F;
    }

    protected float getInaccuracy() {
        return -20.0F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition p_70184_1_) {
        if (!worldObj.isRemote) {
            worldObj.playAuxSFX(2002, new BlockPos(this), 0);
            int i = 3 + worldObj.rand.nextInt(5) + worldObj.rand.nextInt(5);

            while (i > 0) {
                int j = EntityXPOrb.getXPSplit(i);
                i -= j;
                worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, j));
            }

            setDead();
        }
    }
}
