package me.banendy.client.mod.mods.combat;

import me.banendy.client.mod.Mod;
import me.banendy.client.mod.ModManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class AimAssist extends Mod {
    private static float yaw;
    private static float pitch;

    public AimAssist() {
        super("AimAssist", false);
        setKey(Keyboard.KEY_Z);
    }

    public boolean isEnable() {
        return enable;
    }

    public static double[] getDistance(double d, double d2, double d3) {
        double d4 = MathHelper.sqrt_double(d * d + d2 * d2);
        double d5 = Math.atan2(d2, d) * 180.0 / Math.PI - 90.0;
        double d6 = -(Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new double[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float) (d5 - (double) mc.thePlayer.rotationYaw)), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float) (d6 - (double) mc.thePlayer.rotationPitch))};
    }

    public static double[] getRotationsNeeded(Entity entity) {
        if (entity == null) {
            return null;
        }
        EntityPlayerSP entityPlayerSP = mc.thePlayer;
        double d = entity.posX - entityPlayerSP.posX;
        double d2 = entity.posY + (double) entity.getEyeHeight() * 0.9 - (entityPlayerSP.posY + (double) entityPlayerSP.getEyeHeight());
        double d3 = entity.posZ - entityPlayerSP.posZ;
        return getDistance(d, d3, d2);
    }

    @Override
    public void render() {
        List<Entity> entityList = mc.theWorld.getLoadedEntityList();
        for (Entity entity : entityList) {

            if (entity != mc.thePlayer && entity.isEntityAlive() && entity instanceof EntityPlayer && mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword || mc.thePlayer.getHeldItem().getItem() instanceof ItemAxe) && Math.sqrt(mc.thePlayer.getDistanceSqToEntity(entity)) <= 4 && !mc.thePlayer.isOnSameTeam((EntityLivingBase) entity)) {
                double[] dArray = getRotationsNeeded(entity);
                pitch = (float) (dArray[1] + 4.5);
                yaw = (float) dArray[0];
                float f = mc.thePlayer.rotationYaw;
                float f2 = mc.thePlayer.rotationPitch;
                if (f < yaw && yaw - f > 1 && yaw - f < 45) {
                    mc.thePlayer.rotationYaw *= 1.009;
                } else if (f > yaw && f - yaw > 1 && f - yaw < 45) {
                    mc.thePlayer.rotationYaw *= 0.99;
                }
                if (f2 < pitch && pitch - f2 > 1 && pitch - f2 < 35) {
                    mc.thePlayer.rotationPitch += 0.3f;
                } else if (f2 > pitch && f2 - pitch > 1 && f2 - pitch < 35) {
                    mc.thePlayer.rotationPitch -= 0.3f;
                }

            }
        }
    }
}

