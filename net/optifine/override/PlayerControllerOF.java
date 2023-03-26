package net.optifine.override;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class PlayerControllerOF extends PlayerControllerMP {
    private boolean acting;
    private BlockPos lastClickBlockPos;
    private Entity lastClickEntity;

    public PlayerControllerOF(Minecraft mcIn, NetHandlerPlayClient netHandler) {
        super(mcIn, netHandler);
    }

    /**
     * Called when the player is hitting a block with an item.
     */
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        acting = true;
        lastClickBlockPos = loc;
        boolean flag = super.clickBlock(loc, face);
        acting = false;
        return flag;
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        acting = true;
        lastClickBlockPos = posBlock;
        boolean flag = super.onPlayerDamageBlock(posBlock, directionFacing);
        acting = false;
        return flag;
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    public boolean sendUseItem(EntityPlayer player, World worldIn, ItemStack stack) {
        acting = true;
        boolean flag = super.sendUseItem(player, worldIn, stack);
        acting = false;
        return flag;
    }

    public boolean onPlayerRightClick(EntityPlayerSP p_178890_1, WorldClient p_178890_2, ItemStack p_178890_3, BlockPos p_178890_4, EnumFacing p_178890_5, Vec3 p_178890_6) {
        acting = true;
        lastClickBlockPos = p_178890_4;
        boolean flag = super.onPlayerRightClick(p_178890_1, p_178890_2, p_178890_3, p_178890_4, p_178890_5, p_178890_6);
        acting = false;
        return flag;
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    public boolean interactWithEntitySendPacket(EntityPlayer player, Entity target) {
        lastClickEntity = target;
        return super.interactWithEntitySendPacket(player, target);
    }

    /**
     * Return true when the player rightclick on an entity
     *
     * @param player       The player's instance
     * @param entityIn     The entity clicked
     * @param movingObject The object clicked
     */
    public boolean isPlayerRightClickingOnEntity(EntityPlayer player, Entity target, MovingObjectPosition ray) {
        lastClickEntity = target;
        return super.isPlayerRightClickingOnEntity(player, target, ray);
    }

    public boolean isActing() {
        return acting;
    }

    public BlockPos getLastClickBlockPos() {
        return lastClickBlockPos;
    }

    public Entity getLastClickEntity() {
        return lastClickEntity;
    }
}
