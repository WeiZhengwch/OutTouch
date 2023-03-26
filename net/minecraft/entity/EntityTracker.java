package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

public class EntityTracker {
    private static final Logger logger = LogManager.getLogger();
    private final WorldServer theWorld;
    private final Set<EntityTrackerEntry> trackedEntities = Sets.newHashSet();
    private final IntHashMap<EntityTrackerEntry> trackedEntityHashTable = new IntHashMap();
    private final int maxTrackingDistanceThreshold;

    public EntityTracker(WorldServer theWorldIn) {
        theWorld = theWorldIn;
        maxTrackingDistanceThreshold = theWorldIn.getMinecraftServer().getConfigurationManager().getEntityViewDistance();
    }

    public void trackEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayerMP entityplayermp) {
            trackEntity(entityIn, 512, 2);

            for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
                if (entitytrackerentry.trackedEntity != entityplayermp) {
                    entitytrackerentry.updatePlayerEntity(entityplayermp);
                }
            }
        } else if (entityIn instanceof EntityFishHook) {
            addEntityToTracker(entityIn, 64, 5, true);
        } else if (entityIn instanceof EntityArrow) {
            addEntityToTracker(entityIn, 64, 20, false);
        } else if (entityIn instanceof EntitySmallFireball) {
            addEntityToTracker(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntityFireball) {
            addEntityToTracker(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntitySnowball) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityEnderPearl) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityEnderEye) {
            addEntityToTracker(entityIn, 64, 4, true);
        } else if (entityIn instanceof EntityEgg) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityPotion) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityExpBottle) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityFireworkRocket) {
            addEntityToTracker(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityItem) {
            addEntityToTracker(entityIn, 64, 20, true);
        } else if (entityIn instanceof EntityMinecart) {
            addEntityToTracker(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityBoat) {
            addEntityToTracker(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntitySquid) {
            addEntityToTracker(entityIn, 64, 3, true);
        } else if (entityIn instanceof EntityWither) {
            addEntityToTracker(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityBat) {
            addEntityToTracker(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityDragon) {
            addEntityToTracker(entityIn, 160, 3, true);
        } else if (entityIn instanceof IAnimals) {
            addEntityToTracker(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityTNTPrimed) {
            addEntityToTracker(entityIn, 160, 10, true);
        } else if (entityIn instanceof EntityFallingBlock) {
            addEntityToTracker(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityHanging) {
            addEntityToTracker(entityIn, 160, Integer.MAX_VALUE, false);
        } else if (entityIn instanceof EntityArmorStand) {
            addEntityToTracker(entityIn, 160, 3, true);
        } else if (entityIn instanceof EntityXPOrb) {
            addEntityToTracker(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityEnderCrystal) {
            addEntityToTracker(entityIn, 256, Integer.MAX_VALUE, false);
        }
    }

    public void trackEntity(Entity entityIn, int trackingRange, int updateFrequency) {
        addEntityToTracker(entityIn, trackingRange, updateFrequency, false);
    }

    /**
     * Args : Entity, trackingRange, updateFrequency, sendVelocityUpdates
     */
    public void addEntityToTracker(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates) {
        if (trackingRange > maxTrackingDistanceThreshold) {
            trackingRange = maxTrackingDistanceThreshold;
        }

        try {
            if (trackedEntityHashTable.containsItem(entityIn.getEntityId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }

            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entityIn, trackingRange, updateFrequency, sendVelocityUpdates);
            trackedEntities.add(entitytrackerentry);
            trackedEntityHashTable.addKey(entityIn.getEntityId(), entitytrackerentry);
            entitytrackerentry.updatePlayerEntities(theWorld.playerEntities);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding entity to track");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
            crashreportcategory.addCrashSection("Tracking range", trackingRange + " blocks");
            crashreportcategory.addCrashSectionCallable("Update interval", () -> {
                String s = "Once per " + updateFrequency + " ticks";

                if (updateFrequency == Integer.MAX_VALUE) {
                    s = "Maximum (" + s + ")";
                }

                return s;
            });
            entityIn.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Entity That Is Already Tracked");
            trackedEntityHashTable.lookup(entityIn.getEntityId()).trackedEntity.addEntityCrashInfo(crashreportcategory1);

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                logger.error("\"Silently\" catching entity tracking error.", reportedexception);
            }
        }
    }

    public void untrackEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayerMP entityplayermp) {

            for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
                entitytrackerentry.removeFromTrackedPlayers(entityplayermp);
            }
        }

        EntityTrackerEntry entitytrackerentry1 = trackedEntityHashTable.removeObject(entityIn.getEntityId());

        if (entitytrackerentry1 != null) {
            trackedEntities.remove(entitytrackerentry1);
            entitytrackerentry1.sendDestroyEntityPacketToTrackedPlayers();
        }
    }

    public void updateTrackedEntities() {
        List<EntityPlayerMP> list = Lists.newArrayList();

        for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
            entitytrackerentry.updatePlayerList(theWorld.playerEntities);

            if (entitytrackerentry.playerEntitiesUpdated && entitytrackerentry.trackedEntity instanceof EntityPlayerMP) {
                list.add((EntityPlayerMP) entitytrackerentry.trackedEntity);
            }
        }

        for (EntityPlayerMP entityplayermp : list) {
            for (EntityTrackerEntry entitytrackerentry1 : trackedEntities) {
                if (entitytrackerentry1.trackedEntity != entityplayermp) {
                    entitytrackerentry1.updatePlayerEntity(entityplayermp);
                }
            }
        }
    }

    public void func_180245_a(EntityPlayerMP p_180245_1_) {
        for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
            if (entitytrackerentry.trackedEntity == p_180245_1_) {
                entitytrackerentry.updatePlayerEntities(theWorld.playerEntities);
            } else {
                entitytrackerentry.updatePlayerEntity(p_180245_1_);
            }
        }
    }

    public void sendToAllTrackingEntity(Entity entityIn, Packet p_151247_2_) {
        EntityTrackerEntry entitytrackerentry = trackedEntityHashTable.lookup(entityIn.getEntityId());

        if (entitytrackerentry != null) {
            entitytrackerentry.sendPacketToTrackedPlayers(p_151247_2_);
        }
    }

    public void func_151248_b(Entity entityIn, Packet p_151248_2_) {
        EntityTrackerEntry entitytrackerentry = trackedEntityHashTable.lookup(entityIn.getEntityId());

        if (entitytrackerentry != null) {
            entitytrackerentry.func_151261_b(p_151248_2_);
        }
    }

    public void removePlayerFromTrackers(EntityPlayerMP p_72787_1_) {
        for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
            entitytrackerentry.removeTrackedPlayerSymmetric(p_72787_1_);
        }
    }

    public void func_85172_a(EntityPlayerMP p_85172_1_, Chunk p_85172_2_) {
        for (EntityTrackerEntry entitytrackerentry : trackedEntities) {
            if (entitytrackerentry.trackedEntity != p_85172_1_ && entitytrackerentry.trackedEntity.chunkCoordX == p_85172_2_.xPosition && entitytrackerentry.trackedEntity.chunkCoordZ == p_85172_2_.zPosition) {
                entitytrackerentry.updatePlayerEntity(p_85172_1_);
            }
        }
    }
}
