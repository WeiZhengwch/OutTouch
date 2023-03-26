package net.minecraft.entity.monster;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySnowman extends EntityGolem implements IRangedAttackMob {
    public EntitySnowman(World worldIn) {
        super(worldIn);
        setSize(0.7F, 1.9F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(1, new EntityAIArrowAttack(this, 1.25D, 20, 10.0F));
        tasks.addTask(2, new EntityAIWander(this, 1.0D));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(4, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, IMob.mobSelector));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!worldObj.isRemote) {
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY);
            int k = MathHelper.floor_double(posZ);

            if (isWet()) {
                attackEntityFrom(DamageSource.drown, 1.0F);
            }

            if (worldObj.getBiomeGenForCoords(new BlockPos(i, 0, k)).getFloatTemperature(new BlockPos(i, j, k)) > 1.0F) {
                attackEntityFrom(DamageSource.onFire, 1.0F);
            }

            for (int l = 0; l < 4; ++l) {
                i = MathHelper.floor_double(posX + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
                j = MathHelper.floor_double(posY);
                k = MathHelper.floor_double(posZ + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPos blockpos = new BlockPos(i, j, k);

                if (worldObj.getBlockState(blockpos).getBlock().getMaterial() == Material.air && worldObj.getBiomeGenForCoords(new BlockPos(i, 0, k)).getFloatTemperature(blockpos) < 0.8F && Blocks.snow_layer.canPlaceBlockAt(worldObj, blockpos)) {
                    worldObj.setBlockState(blockpos, Blocks.snow_layer.getDefaultState());
                }
            }
        }
    }

    protected Item getDropItem() {
        return Items.snowball;
    }

    /**
     * Drop 0-2 items of this living's type
     *
     * @param wasRecentlyHit  true if this this entity was recently hit by appropriate entity (generally only if player
     *                        or tameable)
     * @param lootingModifier level of enchanment to be applied to this drop
     */
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int i = rand.nextInt(16);

        for (int j = 0; j < i; ++j) {
            dropItem(Items.snowball, 1);
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
        EntitySnowball entitysnowball = new EntitySnowball(worldObj, this);
        double d0 = target.posY + (double) target.getEyeHeight() - 1.100000023841858D;
        double d1 = target.posX - posX;
        double d2 = d0 - entitysnowball.posY;
        double d3 = target.posZ - posZ;
        float f = MathHelper.sqrt_double(d1 * d1 + d3 * d3) * 0.2F;
        entitysnowball.setThrowableHeading(d1, d2 + (double) f, d3, 1.6F, 12.0F);
        playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(entitysnowball);
    }

    public float getEyeHeight() {
        return 1.7F;
    }
}