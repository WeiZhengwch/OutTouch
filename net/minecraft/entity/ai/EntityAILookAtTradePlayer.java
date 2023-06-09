package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAILookAtTradePlayer extends EntityAIWatchClosest {
    private final EntityVillager theMerchant;

    public EntityAILookAtTradePlayer(EntityVillager theMerchantIn) {
        super(theMerchantIn, EntityPlayer.class, 8.0F);
        theMerchant = theMerchantIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (theMerchant.isTrading()) {
            closestEntity = theMerchant.getCustomer();
            return true;
        } else {
            return false;
        }
    }
}
