package net.minecraft.client.multiplayer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerControllerMP {
    /**
     * The Minecraft instance.
     */
    private final Minecraft mc;
    private final NetHandlerPlayClient netClientHandler;
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);

    /**
     * The Item currently being used to destroy a block
     */
    private ItemStack currentItemHittingBlock;

    /**
     * Current block damage (MP)
     */
    private float curBlockDamageMP;

    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    private float stepSoundTickCounter;

    /**
     * Delays the first damage on the block after the first click on the block
     */
    private int blockHitDelay;

    /**
     * Tells if the player is hitting a block
     */
    private boolean isHittingBlock;

    /**
     * Current game type for the player
     */
    private WorldSettings.GameType currentGameType = WorldSettings.GameType.SURVIVAL;

    /**
     * Index of the current item held by the player in the inventory hotbar
     */
    private int currentPlayerItem;

    public PlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {
        mc = mcIn;
        netClientHandler = netHandler;
    }

    public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing) {
        if (!mcIn.theWorld.extinguishFire(mcIn.thePlayer, pos, facing)) {
            playerController.onPlayerDestroyBlock(pos, facing);
        }
    }

    /**
     * Sets player capabilities depending on current gametype. params: player
     *
     * @param player The player's instance
     */
    public void setPlayerCapabilities(EntityPlayer player) {
        currentGameType.configurePlayerCapabilities(player.capabilities);
    }

    /**
     * None
     */
    public boolean isSpectator() {
        return currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    /**
     * Sets the game type for the player.
     *
     * @param type The GameType to set
     */
    public void setGameType(WorldSettings.GameType type) {
        currentGameType = type;
        currentGameType.configurePlayerCapabilities(mc.thePlayer.capabilities);
    }

    /**
     * Flips the player around.
     */
    public void flipPlayer(EntityPlayer playerIn) {
        playerIn.rotationYaw = -180.0F;
    }

    public boolean shouldDrawHUD() {
        return currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Called when a player completes the destruction of a block
     */
    public boolean onPlayerDestroyBlock(BlockPos pos, EnumFacing side) {
        if (currentGameType.isAdventure()) {
            if (currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!mc.thePlayer.isAllowEdit()) {
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (currentGameType.isCreative() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            return false;
        } else {
            World world = mc.theWorld;
            IBlockState iblockstate = world.getBlockState(pos);
            Block block1 = iblockstate.getBlock();

            if (block1.getMaterial() == Material.air) {
                return false;
            } else {
                world.playAuxSFX(2001, pos, Block.getStateId(iblockstate));
                boolean flag = world.setBlockToAir(pos);

                if (flag) {
                    block1.onBlockDestroyedByPlayer(world, pos, iblockstate);
                }

                currentBlock = new BlockPos(currentBlock.getX(), -1, currentBlock.getZ());

                if (!currentGameType.isCreative()) {
                    ItemStack itemstack1 = mc.thePlayer.getCurrentEquippedItem();

                    if (itemstack1 != null) {
                        itemstack1.onBlockDestroyed(world, block1, pos, mc.thePlayer);

                        if (itemstack1.stackSize == 0) {
                            mc.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                }

                return flag;
            }
        }
    }

    /**
     * Called when the player is hitting a block with an item.
     */
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        if (currentGameType.isAdventure()) {
            if (currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!mc.thePlayer.isAllowEdit()) {
                Block block = mc.theWorld.getBlockState(loc).getBlock();
                ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (!mc.theWorld.getWorldBorder().contains(loc)) {
            return false;
        } else {
            if (currentGameType.isCreative()) {
                netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                clickBlockCreative(mc, this, loc, face);
                blockHitDelay = 5;
            } else if (!isHittingBlock || !isHittingPosition(loc)) {
                if (isHittingBlock) {
                    netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, face));
                }

                netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                Block block1 = mc.theWorld.getBlockState(loc).getBlock();
                boolean flag = block1.getMaterial() != Material.air;

                if (flag && curBlockDamageMP == 0.0F) {
                    block1.onBlockClicked(mc.theWorld, loc, mc.thePlayer);
                }

                if (flag && block1.getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, loc) >= 1.0F) {
                    onPlayerDestroyBlock(loc, face);
                } else {
                    isHittingBlock = true;
                    currentBlock = loc;
                    currentItemHittingBlock = mc.thePlayer.getHeldItem();
                    curBlockDamageMP = 0.0F;
                    stepSoundTickCounter = 0.0F;
                    mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, (int) (curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    public void resetBlockRemoving() {
        if (isHittingBlock) {
            netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
            isHittingBlock = false;
            curBlockDamageMP = 0.0F;
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, -1);
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        syncCurrentPlayItem();

        if (blockHitDelay > 0) {
            --blockHitDelay;
            return true;
        } else if (currentGameType.isCreative() && mc.theWorld.getWorldBorder().contains(posBlock)) {
            blockHitDelay = 5;
            netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(mc, this, posBlock, directionFacing);
            return true;
        } else if (isHittingPosition(posBlock)) {
            Block block = mc.theWorld.getBlockState(posBlock).getBlock();

            if (block.getMaterial() == Material.air) {
                isHittingBlock = false;
                return false;
            } else {
                curBlockDamageMP += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, posBlock);

                if (stepSoundTickCounter % 4.0F == 0.0F) {
                    mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepSound()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getFrequency() * 0.5F, (float) posBlock.getX() + 0.5F, (float) posBlock.getY() + 0.5F, (float) posBlock.getZ() + 0.5F));
                }

                ++stepSoundTickCounter;

                if (curBlockDamageMP >= 1.0F) {
                    isHittingBlock = false;
                    netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    onPlayerDestroyBlock(posBlock, directionFacing);
                    curBlockDamageMP = 0.0F;
                    stepSoundTickCounter = 0.0F;
                    blockHitDelay = 5;
                }

                mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, (int) (curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        } else {
            return clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * player reach distance = 4F
     */
    public float getBlockReachDistance() {
        return currentGameType.isCreative() ? 5.0F : 4.5F;
    }

    public void updateController() {
        syncCurrentPlayItem();

        if (netClientHandler.getNetworkManager().isChannelOpen()) {
            netClientHandler.getNetworkManager().processReceivedPackets();
        } else {
            netClientHandler.getNetworkManager().checkDisconnected();
        }
    }

    private boolean isHittingPosition(BlockPos pos) {
        ItemStack itemstack = mc.thePlayer.getHeldItem();
        boolean flag = currentItemHittingBlock == null && itemstack == null;

        if (currentItemHittingBlock != null && itemstack != null) {
            flag = itemstack.getItem() == currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == currentItemHittingBlock.getMetadata());
        }

        return pos.equals(currentBlock) && flag;
    }

    /**
     * Syncs the current player item with the server
     */
    private void syncCurrentPlayItem() {
        int i = mc.thePlayer.inventory.currentItem;

        if (i != currentPlayerItem) {
            currentPlayerItem = i;
            netClientHandler.addToSendQueue(new C09PacketHeldItemChange(currentPlayerItem));
        }
    }

    public boolean onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec) {
        syncCurrentPlayItem();
        float f = (float) (hitVec.xCoord - (double) hitPos.getX());
        float f1 = (float) (hitVec.yCoord - (double) hitPos.getY());
        float f2 = (float) (hitVec.zCoord - (double) hitPos.getZ());
        boolean flag = false;

        if (!mc.theWorld.getWorldBorder().contains(hitPos)) {
            return false;
        } else {
            if (currentGameType != WorldSettings.GameType.SPECTATOR) {
                IBlockState iblockstate = worldIn.getBlockState(hitPos);

                if ((!player.isSneaking() || player.getHeldItem() == null) && iblockstate.getBlock().onBlockActivated(worldIn, hitPos, iblockstate, player, side, f, f1, f2)) {
                    flag = true;
                }

                if (!flag && heldStack != null && heldStack.getItem() instanceof ItemBlock itemblock) {

                    if (!itemblock.canPlaceBlockOnSide(worldIn, hitPos, side, player, heldStack)) {
                        return false;
                    }
                }
            }

            netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(hitPos, side.getIndex(), player.inventory.getCurrentItem(), f, f1, f2));

            if (!flag && currentGameType != WorldSettings.GameType.SPECTATOR) {
                if (heldStack == null) {
                    return false;
                } else if (currentGameType.isCreative()) {
                    int i = heldStack.getMetadata();
                    int j = heldStack.stackSize;
                    boolean flag1 = heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                    heldStack.setItemDamage(i);
                    heldStack.stackSize = j;
                    return flag1;
                } else {
                    return heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                }
            } else {
                return true;
            }
        }
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    public boolean sendUseItem(EntityPlayer playerIn, World worldIn, ItemStack itemStackIn) {
        if (currentGameType == WorldSettings.GameType.SPECTATOR) {
            return false;
        } else {
            syncCurrentPlayItem();
            netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(playerIn.inventory.getCurrentItem()));
            int i = itemStackIn.stackSize;
            ItemStack itemstack = itemStackIn.useItemRightClick(worldIn, playerIn);

            if (itemstack != itemStackIn || itemstack.stackSize != i) {
                playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0) {
                    playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = null;
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public EntityPlayerSP func_178892_a(World worldIn, StatFileWriter statWriter) {
        return new EntityPlayerSP(mc, worldIn, netClientHandler, statWriter);
    }

    /**
     * Attacks an entity
     */
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {
        syncCurrentPlayItem();
        netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.ATTACK));

        if (currentGameType != WorldSettings.GameType.SPECTATOR) {
            playerIn.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    public boolean interactWithEntitySendPacket(EntityPlayer playerIn, Entity targetEntity) {
        syncCurrentPlayItem();
        netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
        return currentGameType != WorldSettings.GameType.SPECTATOR && playerIn.interactWith(targetEntity);
    }

    /**
     * Return true when the player rightclick on an entity
     *
     * @param player       The player's instance
     * @param entityIn     The entity clicked
     * @param movingObject The object clicked
     */
    public boolean isPlayerRightClickingOnEntity(EntityPlayer player, Entity entityIn, MovingObjectPosition movingObject) {
        syncCurrentPlayItem();
        Vec3 vec3 = new Vec3(movingObject.hitVec.xCoord - entityIn.posX, movingObject.hitVec.yCoord - entityIn.posY, movingObject.hitVec.zCoord - entityIn.posZ);
        netClientHandler.addToSendQueue(new C02PacketUseEntity(entityIn, vec3));
        return currentGameType != WorldSettings.GameType.SPECTATOR && entityIn.interactAt(player, vec3);
    }

    /**
     * Handles slot clicks sends a packet to the server.
     */
    public ItemStack windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn) {
        short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
        ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, mode, playerIn);
        netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, mode, itemstack, short1));
        return itemstack;
    }

    /**
     * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
     * enchantment action the player has taken.
     *
     * @param windowID The ID of the current window
     * @param button   The button id (enchantment selected)
     */
    public void sendEnchantPacket(int windowID, int button) {
        netClientHandler.addToSendQueue(new C11PacketEnchantItem(windowID, button));
    }

    /**
     * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
     */
    public void sendSlotPacket(ItemStack itemStackIn, int slotId) {
        if (currentGameType.isCreative()) {
            netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(slotId, itemStackIn));
        }
    }

    /**
     * Sends a Packet107 to the server to drop the item on the ground
     */
    public void sendPacketDropItem(ItemStack itemStackIn) {
        if (currentGameType.isCreative() && itemStackIn != null) {
            netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(-1, itemStackIn));
        }
    }

    public void onStoppedUsingItem(EntityPlayer playerIn) {
        syncCurrentPlayItem();
        netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        playerIn.stopUsingItem();
    }

    public boolean gameIsSurvivalOrAdventure() {
        return currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative() {
        return !currentGameType.isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode() {
        return currentGameType.isCreative();
    }

    /**
     * true for hitting entities far away.
     */
    public boolean extendedReach() {
        return currentGameType.isCreative();
    }

    /**
     * Checks if the player is riding a horse, used to chose the GUI to open
     */
    public boolean isRidingHorse() {
        return mc.thePlayer.isRiding() && mc.thePlayer.ridingEntity instanceof EntityHorse;
    }

    public boolean isSpectatorMode() {
        return currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    public WorldSettings.GameType getCurrentGameType() {
        return currentGameType;
    }

    /**
     * Return isHittingBlock
     */
    public boolean getIsHittingBlock() {
        return isHittingBlock;
    }
}
