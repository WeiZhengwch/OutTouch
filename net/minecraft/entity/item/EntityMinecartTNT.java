package net.minecraft.entity.item;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityMinecartTNT extends EntityMinecart {
    private int minecartTNTFuse = -1;

    public EntityMinecartTNT(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartTNT(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public EntityMinecart.EnumMinecartType getMinecartType() {
        return EntityMinecart.EnumMinecartType.TNT;
    }

    public IBlockState getDefaultDisplayTile() {
        return Blocks.tnt.getDefaultState();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        super.onUpdate();

        if (minecartTNTFuse > 0) {
            --minecartTNTFuse;
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
        } else if (minecartTNTFuse == 0) {
            explodeCart(motionX * motionX + motionZ * motionZ);
        }

        if (isCollidedHorizontally) {
            double d0 = motionX * motionX + motionZ * motionZ;

            if (d0 >= 0.009999999776482582D) {
                explodeCart(d0);
            }
        }
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity entity = source.getSourceOfDamage();

        if (entity instanceof EntityArrow entityarrow) {

            if (entityarrow.isBurning()) {
                explodeCart(entityarrow.motionX * entityarrow.motionX + entityarrow.motionY * entityarrow.motionY + entityarrow.motionZ * entityarrow.motionZ);
            }
        }

        return super.attackEntityFrom(source, amount);
    }

    public void killMinecart(DamageSource source) {
        super.killMinecart(source);
        double d0 = motionX * motionX + motionZ * motionZ;

        if (!source.isExplosion() && worldObj.getGameRules().getBoolean("doEntityDrops")) {
            entityDropItem(new ItemStack(Blocks.tnt, 1), 0.0F);
        }

        if (source.isFireDamage() || source.isExplosion() || d0 >= 0.009999999776482582D) {
            explodeCart(d0);
        }
    }

    /**
     * Makes the minecart explode.
     */
    protected void explodeCart(double p_94103_1_) {
        if (!worldObj.isRemote) {
            double d0 = Math.sqrt(p_94103_1_);

            if (d0 > 5.0D) {
                d0 = 5.0D;
            }

            worldObj.createExplosion(this, posX, posY, posZ, (float) (4.0D + rand.nextDouble() * 1.5D * d0), true);
            setDead();
        }
    }

    public void fall(float distance, float damageMultiplier) {
        if (distance >= 3.0F) {
            float f = distance / 10.0F;
            explodeCart(f * f);
        }

        super.fall(distance, damageMultiplier);
    }

    /**
     * Called every tick the minecart is on an activator rail. Args: x, y, z, is the rail receiving power
     */
    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        if (receivingPower && minecartTNTFuse < 0) {
            ignite();
        }
    }

    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            ignite();
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Ignites this TNT cart.
     */
    public void ignite() {
        minecartTNTFuse = 80;

        if (!worldObj.isRemote) {
            worldObj.setEntityState(this, (byte) 10);

            if (!isSilent()) {
                worldObj.playSoundAtEntity(this, "game.tnt.primed", 1.0F, 1.0F);
            }
        }
    }

    /**
     * Gets the remaining fuse time in ticks.
     */
    public int getFuseTicks() {
        return minecartTNTFuse;
    }

    /**
     * Returns true if the TNT minecart is ignited.
     */
    public boolean isIgnited() {
        return minecartTNTFuse > -1;
    }

    /**
     * Explosion resistance of a block relative to this entity
     */
    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        return !isIgnited() || !BlockRailBase.isRailBlock(blockStateIn) && !BlockRailBase.isRailBlock(worldIn, pos.up()) ? super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn) : 0.0F;
    }

    public boolean verifyExplosion(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn, float p_174816_5_) {
        return (!isIgnited() || !BlockRailBase.isRailBlock(blockStateIn) && !BlockRailBase.isRailBlock(worldIn, pos.up())) && super.verifyExplosion(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("TNTFuse", 99)) {
            minecartTNTFuse = tagCompund.getInteger("TNTFuse");
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("TNTFuse", minecartTNTFuse);
    }
}
