package net.minecraft.entity.monster;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityMagmaCube extends EntitySlime {
    public EntityMagmaCube(World worldIn) {
        super(worldIn);
        isImmuneToFire = true;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere() {
        return worldObj.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding() {
        return worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(getEntityBoundingBox());
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue() {
        return getSlimeSize() * 3;
    }

    public int getBrightnessForRender(float partialTicks) {
        return 15728880;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks) {
        return 1.0F;
    }

    protected EnumParticleTypes getParticleType() {
        return EnumParticleTypes.FLAME;
    }

    protected EntitySlime createInstance() {
        return new EntityMagmaCube(worldObj);
    }

    protected Item getDropItem() {
        return Items.magma_cream;
    }

    /**
     * Drop 0-2 items of this living's type
     *
     * @param wasRecentlyHit  true if this this entity was recently hit by appropriate entity (generally only if player
     *                        or tameable)
     * @param lootingModifier level of enchanment to be applied to this drop
     */
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        Item item = getDropItem();

        if (item != null && getSlimeSize() > 1) {
            int i = rand.nextInt(4) - 2;

            if (lootingModifier > 0) {
                i += rand.nextInt(lootingModifier + 1);
            }

            for (int j = 0; j < i; ++j) {
                dropItem(item, 1);
            }
        }
    }

    /**
     * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    public boolean isBurning() {
        return false;
    }

    /**
     * Gets the amount of time the slime needs to wait between jumps.
     */
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    protected void alterSquishAmount() {
        squishAmount *= 0.9F;
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    protected void jump() {
        motionY = 0.42F + (float) getSlimeSize() * 0.1F;
        isAirBorne = true;
    }

    protected void handleJumpLava() {
        motionY = 0.22F + (float) getSlimeSize() * 0.05F;
        isAirBorne = true;
    }

    public void fall(float distance, float damageMultiplier) {
    }

    /**
     * Indicates weather the slime is able to damage the player (based upon the slime's size)
     */
    protected boolean canDamagePlayer() {
        return true;
    }

    /**
     * Gets the amount of damage dealt to the player when "attacked" by the slime.
     */
    protected int getAttackStrength() {
        return super.getAttackStrength() + 2;
    }

    /**
     * Returns the name of the sound played when the slime jumps.
     */
    protected String getJumpSound() {
        return getSlimeSize() > 1 ? "mob.magmacube.big" : "mob.magmacube.small";
    }

    /**
     * Returns true if the slime makes a sound when it lands after a jump (based upon the slime's size)
     */
    protected boolean makesSoundOnLand() {
        return true;
    }
}
