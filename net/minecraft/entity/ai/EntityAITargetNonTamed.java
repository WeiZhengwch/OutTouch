package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAITargetNonTamed<T extends EntityLivingBase> extends EntityAINearestAttackableTarget {
    private final EntityTameable theTameable;

    public EntityAITargetNonTamed(EntityTameable entityIn, Class<T> classTarget, boolean checkSight, Predicate<? super T> targetSelector) {
        super(entityIn, classTarget, 10, checkSight, false, targetSelector);
        theTameable = entityIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        return !theTameable.isTamed() && super.shouldExecute();
    }
}
