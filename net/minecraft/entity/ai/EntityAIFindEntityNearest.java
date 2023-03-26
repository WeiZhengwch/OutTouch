package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class EntityAIFindEntityNearest extends EntityAIBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private final EntityLiving mob;
    private final Predicate<EntityLivingBase> field_179443_c;
    private final EntityAINearestAttackableTarget.Sorter field_179440_d;
    private final Class<? extends EntityLivingBase> field_179439_f;
    private EntityLivingBase target;

    public EntityAIFindEntityNearest(EntityLiving mobIn, Class<? extends EntityLivingBase> p_i45884_2_) {
        mob = mobIn;
        field_179439_f = p_i45884_2_;

        if (mobIn instanceof EntityCreature) {
            LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
        }

        field_179443_c = p_apply_1_ -> {
            double d0 = getFollowRange();

            if (p_apply_1_.isSneaking()) {
                d0 *= 0.800000011920929D;
            }

            return !p_apply_1_.isInvisible() && (!((double) p_apply_1_.getDistanceToEntity(mob) > d0) && EntityAITarget.isSuitableTarget(mob, p_apply_1_, false, true));
        };
        field_179440_d = new EntityAINearestAttackableTarget.Sorter(mobIn);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        double d0 = getFollowRange();
        List<EntityLivingBase> list = mob.worldObj.getEntitiesWithinAABB(field_179439_f, mob.getEntityBoundingBox().expand(d0, 4.0D, d0), field_179443_c);
        list.sort(field_179440_d);

        if (list.isEmpty()) {
            return false;
        } else {
            target = list.get(0);
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        EntityLivingBase entitylivingbase = mob.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else {
            double d0 = getFollowRange();
            return !(mob.getDistanceSqToEntity(entitylivingbase) > d0 * d0) && (!(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP) entitylivingbase).theItemInWorldManager.isCreative());
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        mob.setAttackTarget(target);
        super.startExecuting();
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        mob.setAttackTarget(null);
        super.startExecuting();
    }

    protected double getFollowRange() {
        IAttributeInstance iattributeinstance = mob.getEntityAttribute(SharedMonsterAttributes.followRange);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }
}
