package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityOcelot extends EntityTameable {
    /**
     * The tempt AI task for this mob, used to prevent taming while it is fleeing.
     */
    private final EntityAITempt aiTempt;
    private EntityAIAvoidEntity<EntityPlayer> avoidEntity;

    public EntityOcelot(World worldIn) {
        super(worldIn);
        setSize(0.6F, 0.7F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, aiSit);
        tasks.addTask(3, aiTempt = new EntityAITempt(this, 0.6D, Items.fish, true));
        tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
        tasks.addTask(6, new EntityAIOcelotSit(this, 0.8D));
        tasks.addTask(7, new EntityAILeapAtTarget(this, 0.3F));
        tasks.addTask(8, new EntityAIOcelotAttack(this));
        tasks.addTask(9, new EntityAIMate(this, 0.8D));
        tasks.addTask(10, new EntityAIWander(this, 0.8D));
        tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        targetTasks.addTask(1, new EntityAITargetNonTamed(this, EntityChicken.class, false, null));
    }

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(18, (byte) 0);
    }

    public void updateAITasks() {
        if (getMoveHelper().isUpdating()) {
            double d0 = getMoveHelper().getSpeed();

            if (d0 == 0.6D) {
                setSneaking(true);
                setSprinting(false);
            } else if (d0 == 1.33D) {
                setSneaking(false);
                setSprinting(true);
            } else {
                setSneaking(false);
                setSprinting(false);
            }
        } else {
            setSneaking(false);
            setSprinting(false);
        }
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn() {
        return !isTamed() && ticksExisted > 2400;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
    }

    public void fall(float distance, float damageMultiplier) {
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("CatType", getTameSkin());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        setTameSkin(tagCompund.getInteger("CatType"));
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound() {
        return isTamed() ? (isInLove() ? "mob.cat.purr" : (rand.nextInt(4) == 0 ? "mob.cat.purreow" : "mob.cat.meow")) : "";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound() {
        return "mob.cat.hitt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound() {
        return "mob.cat.hitt";
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume() {
        return 0.4F;
    }

    protected Item getDropItem() {
        return Items.leather;
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            aiSit.setSitting(false);
            return super.attackEntityFrom(source, amount);
        }
    }

    /**
     * Drop 0-2 items of this living's type
     *
     * @param wasRecentlyHit  true if this this entity was recently hit by appropriate entity (generally only if player
     *                        or tameable)
     * @param lootingModifier level of enchanment to be applied to this drop
     */
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (isTamed()) {
            if (isOwner(player) && !worldObj.isRemote && !isBreedingItem(itemstack)) {
                aiSit.setSitting(!isSitting());
            }
        } else if (aiTempt.isRunning() && itemstack != null && itemstack.getItem() == Items.fish && player.getDistanceSqToEntity(this) < 9.0D) {
            if (!player.capabilities.isCreativeMode) {
                --itemstack.stackSize;
            }

            if (itemstack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            if (!worldObj.isRemote) {
                if (rand.nextInt(3) == 0) {
                    setTamed(true);
                    setTameSkin(1 + worldObj.rand.nextInt(3));
                    setOwnerId(player.getUniqueID().toString());
                    playTameEffect(true);
                    aiSit.setSitting(true);
                    worldObj.setEntityState(this, (byte) 7);
                } else {
                    playTameEffect(false);
                    worldObj.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.interact(player);
    }

    public EntityOcelot createChild(EntityAgeable ageable) {
        EntityOcelot entityocelot = new EntityOcelot(worldObj);

        if (isTamed()) {
            entityocelot.setOwnerId(getOwnerId());
            entityocelot.setTamed(true);
            entityocelot.setTameSkin(getTameSkin());
        }

        return entityocelot;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == Items.fish;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        } else if (!isTamed()) {
            return false;
        } else if (!(otherAnimal instanceof EntityOcelot entityocelot)) {
            return false;
        } else {
            return entityocelot.isTamed() && isInLove() && entityocelot.isInLove();
        }
    }

    public int getTameSkin() {
        return dataWatcher.getWatchableObjectByte(18);
    }

    public void setTameSkin(int skinId) {
        dataWatcher.updateObject(18, (byte) skinId);
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere() {
        return worldObj.rand.nextInt(3) != 0;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding() {
        if (worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(getEntityBoundingBox())) {
            BlockPos blockpos = new BlockPos(posX, getEntityBoundingBox().minY, posZ);

            if (blockpos.getY() < worldObj.getSeaLevel()) {
                return false;
            }

            Block block = worldObj.getBlockState(blockpos.down()).getBlock();

            return block == Blocks.grass || block.getMaterial() == Material.leaves;
        }

        return false;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return hasCustomName() ? getCustomNameTag() : (isTamed() ? StatCollector.translateToLocal("entity.Cat.name") : super.getName());
    }

    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
    }

    protected void setupTamedAI() {
        if (avoidEntity == null) {
            avoidEntity = new EntityAIAvoidEntity(this, EntityPlayer.class, 16.0F, 0.8D, 1.33D);
        }

        tasks.removeTask(avoidEntity);

        if (!isTamed()) {
            tasks.addTask(4, avoidEntity);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        if (worldObj.rand.nextInt(7) == 0) {
            for (int i = 0; i < 2; ++i) {
                EntityOcelot entityocelot = new EntityOcelot(worldObj);
                entityocelot.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
                entityocelot.setGrowingAge(-24000);
                worldObj.spawnEntityInWorld(entityocelot);
            }
        }

        return livingdata;
    }
}
