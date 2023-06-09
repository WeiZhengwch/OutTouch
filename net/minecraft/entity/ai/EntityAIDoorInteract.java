package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;

public abstract class EntityAIDoorInteract extends EntityAIBase {
    protected EntityLiving theEntity;
    protected BlockPos doorPosition = BlockPos.ORIGIN;

    /**
     * The wooden door block
     */
    protected BlockDoor doorBlock;

    /**
     * If is true then the Entity has stopped Door Interaction and compoleted the task.
     */
    boolean hasStoppedDoorInteraction;
    float entityPositionX;
    float entityPositionZ;

    public EntityAIDoorInteract(EntityLiving entityIn) {
        theEntity = entityIn;

        if (!(entityIn.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!theEntity.isCollidedHorizontally) {
            return false;
        } else {
            PathNavigateGround pathnavigateground = (PathNavigateGround) theEntity.getNavigator();
            PathEntity pathentity = pathnavigateground.getPath();

            if (pathentity != null && !pathentity.isFinished() && pathnavigateground.getEnterDoors()) {
                for (int i = 0; i < Math.min(pathentity.getCurrentPathIndex() + 2, pathentity.getCurrentPathLength()); ++i) {
                    PathPoint pathpoint = pathentity.getPathPointFromIndex(i);
                    doorPosition = new BlockPos(pathpoint.xCoord, pathpoint.yCoord + 1, pathpoint.zCoord);

                    if (theEntity.getDistanceSq(doorPosition.getX(), theEntity.posY, doorPosition.getZ()) <= 2.25D) {
                        doorBlock = getBlockDoor(doorPosition);

                        if (doorBlock != null) {
                            return true;
                        }
                    }
                }

                doorPosition = (new BlockPos(theEntity)).up();
                doorBlock = getBlockDoor(doorPosition);
                return doorBlock != null;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !hasStoppedDoorInteraction;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        hasStoppedDoorInteraction = false;
        entityPositionX = (float) ((double) ((float) doorPosition.getX() + 0.5F) - theEntity.posX);
        entityPositionZ = (float) ((double) ((float) doorPosition.getZ() + 0.5F) - theEntity.posZ);
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        float f = (float) ((double) ((float) doorPosition.getX() + 0.5F) - theEntity.posX);
        float f1 = (float) ((double) ((float) doorPosition.getZ() + 0.5F) - theEntity.posZ);
        float f2 = entityPositionX * f + entityPositionZ * f1;

        if (f2 < 0.0F) {
            hasStoppedDoorInteraction = true;
        }
    }

    private BlockDoor getBlockDoor(BlockPos pos) {
        Block block = theEntity.worldObj.getBlockState(pos).getBlock();
        return block instanceof BlockDoor && block.getMaterial() == Material.wood ? (BlockDoor) block : null;
    }
}
