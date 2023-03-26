package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Random;

public class EntityAIFleeSun extends EntityAIBase {
    private final EntityCreature theCreature;
    private final double movementSpeed;
    private final World theWorld;
    private double shelterX;
    private double shelterY;
    private double shelterZ;

    public EntityAIFleeSun(EntityCreature theCreatureIn, double movementSpeedIn) {
        theCreature = theCreatureIn;
        movementSpeed = movementSpeedIn;
        theWorld = theCreatureIn.worldObj;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!theWorld.isDaytime()) {
            return false;
        } else if (!theCreature.isBurning()) {
            return false;
        } else if (!theWorld.canSeeSky(new BlockPos(theCreature.posX, theCreature.getEntityBoundingBox().minY, theCreature.posZ))) {
            return false;
        } else {
            Vec3 vec3 = findPossibleShelter();

            if (vec3 == null) {
                return false;
            } else {
                shelterX = vec3.xCoord;
                shelterY = vec3.yCoord;
                shelterZ = vec3.zCoord;
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !theCreature.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        theCreature.getNavigator().tryMoveToXYZ(shelterX, shelterY, shelterZ, movementSpeed);
    }

    private Vec3 findPossibleShelter() {
        Random random = theCreature.getRNG();
        BlockPos blockpos = new BlockPos(theCreature.posX, theCreature.getEntityBoundingBox().minY, theCreature.posZ);

        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

            if (!theWorld.canSeeSky(blockpos1) && theCreature.getBlockPathWeight(blockpos1) < 0.0F) {
                return new Vec3(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
            }
        }

        return null;
    }
}
