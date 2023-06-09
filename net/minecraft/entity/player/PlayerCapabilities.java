package net.minecraft.entity.player;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerCapabilities {
    /**
     * Disables player damage.
     */
    public boolean disableDamage;

    /**
     * Sets/indicates whether the player is flying.
     */
    public boolean isFlying;

    /**
     * whether or not to allow the player to fly when they double jump.
     */
    public boolean allowFlying;

    /**
     * Used to determine if creative mode is enabled, and therefore if items should be depleted on usage
     */
    public boolean isCreativeMode;

    /**
     * Indicates whether the player is allowed to modify the surroundings
     */
    public boolean allowEdit = true;
    private float flySpeed = 0.05F;
    private float walkSpeed = 0.1F;

    public void writeCapabilitiesToNBT(NBTTagCompound tagCompound) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setBoolean("invulnerable", disableDamage);
        nbttagcompound.setBoolean("flying", isFlying);
        nbttagcompound.setBoolean("mayfly", allowFlying);
        nbttagcompound.setBoolean("instabuild", isCreativeMode);
        nbttagcompound.setBoolean("mayBuild", allowEdit);
        nbttagcompound.setFloat("flySpeed", flySpeed);
        nbttagcompound.setFloat("walkSpeed", walkSpeed);
        tagCompound.setTag("abilities", nbttagcompound);
    }

    public void readCapabilitiesFromNBT(NBTTagCompound tagCompound) {
        if (tagCompound.hasKey("abilities", 10)) {
            NBTTagCompound nbttagcompound = tagCompound.getCompoundTag("abilities");
            disableDamage = nbttagcompound.getBoolean("invulnerable");
            isFlying = nbttagcompound.getBoolean("flying");
            allowFlying = nbttagcompound.getBoolean("mayfly");
            isCreativeMode = nbttagcompound.getBoolean("instabuild");

            if (nbttagcompound.hasKey("flySpeed", 99)) {
                flySpeed = nbttagcompound.getFloat("flySpeed");
                walkSpeed = nbttagcompound.getFloat("walkSpeed");
            }

            if (nbttagcompound.hasKey("mayBuild", 1)) {
                allowEdit = nbttagcompound.getBoolean("mayBuild");
            }
        }
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(float speed) {
        flySpeed = speed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public void setPlayerWalkSpeed(float speed) {
        walkSpeed = speed;
    }
}
