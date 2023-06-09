package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.banendy.client.gui.MainMenu;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.MetadataCombat;
import net.minecraft.client.stream.MetadataPlayerDeath;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.scoreboard.*;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("ALL")
public class NetHandlerPlayClient implements INetHandlerPlayClient {
    private static final Logger logger = LogManager.getLogger();

    /**
     * The NetworkManager instance used to communicate with the server (used only by handlePlayerPosLook to update
     * positioning and handleJoinGame to inform the server of the client distribution/mods)
     */
    private final NetworkManager netManager;
    private final GameProfile profile;

    /**
     * Seems to be either null (integrated server) or an instance of either GuiMultiplayer (when connecting to a server)
     * or GuiScreenReamlsTOS (when connecting to MCO server)
     */
    private final GuiScreen guiScreenServer;
    private final Map<UUID, NetworkPlayerInfo> playerInfoMap = Maps.newHashMap();
    /**
     * Just an ordinary random number generator, used to randomize audio pitch of item/orb pickup and randomize both
     * particlespawn offset and velocity
     */
    private final Random avRandomizer = new Random();
    public int currentServerMaxPlayers = 20;
    /**
     * Reference to the Minecraft instance, which many handler methods operate on
     */
    private Minecraft gameController;
    /**
     * Reference to the current ClientWorld instance, which many handler methods operate on
     */
    private WorldClient clientWorldController;
    /**
     * True if the client has finished downloading terrain and may spawn. Set upon receipt of S08PacketPlayerPosLook,
     * reset upon respawning
     */
    private boolean doneLoadingTerrain;
    private boolean field_147308_k = false;

    public NetHandlerPlayClient(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager p_i46300_3_, GameProfile p_i46300_4_) {
        gameController = mcIn;
        guiScreenServer = p_i46300_2_;
        netManager = p_i46300_3_;
        profile = p_i46300_4_;
    }

    /**
     * Clears the WorldClient instance associated with this NetHandlerPlayClient
     */
    public void cleanup() {
        clientWorldController = null;
    }

    /**
     * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
     * WorldClient and sets the player initial dimension
     */
    public void handleJoinGame(S01PacketJoinGame packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.playerController = new PlayerControllerMP(gameController, this);
        clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), gameController.mcProfiler);
        gameController.gameSettings.difficulty = packetIn.getDifficulty();
        gameController.loadWorld(clientWorldController);
        gameController.thePlayer.dimension = packetIn.getDimension();
//        gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        gameController.thePlayer.setEntityId(packetIn.getEntityId());
        currentServerMaxPlayers = packetIn.getMaxPlayers();
        gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        gameController.playerController.setGameType(packetIn.getGameType());
        gameController.gameSettings.sendSettingsToServer();
        netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
    }

    /**
     * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
     */
    public void handleSpawnObject(S0EPacketSpawnObject packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        double d0 = (double) packetIn.getX() / 32.0D;
        double d1 = (double) packetIn.getY() / 32.0D;
        double d2 = (double) packetIn.getZ() / 32.0D;
        Entity entity = null;

        switch (packetIn.getType()) {
            case 10 ->
                    entity = EntityMinecart.getMinecart(clientWorldController, d0, d1, d2, EntityMinecart.EnumMinecartType.byNetworkID(packetIn.func_149009_m()));
            case 90 -> {
                Entity entity1 = clientWorldController.getEntityByID(packetIn.func_149009_m());
                if (entity1 instanceof EntityPlayer) {
                    entity = new EntityFishHook(clientWorldController, d0, d1, d2, (EntityPlayer) entity1);
                }
                packetIn.func_149002_g(0);
            }
            case 60 -> entity = new EntityArrow(clientWorldController, d0, d1, d2);
            case 61 -> entity = new EntitySnowball(clientWorldController, d0, d1, d2);
            case 71 -> {
                entity = new EntityItemFrame(clientWorldController, new BlockPos(MathHelper.floor_double(d0), MathHelper.floor_double(d1), MathHelper.floor_double(d2)), EnumFacing.getHorizontal(packetIn.func_149009_m()));
                packetIn.func_149002_g(0);
            }
            case 77 -> {
                entity = new EntityLeashKnot(clientWorldController, new BlockPos(MathHelper.floor_double(d0), MathHelper.floor_double(d1), MathHelper.floor_double(d2)));
                packetIn.func_149002_g(0);
            }
            case 65 -> entity = new EntityEnderPearl(clientWorldController, d0, d1, d2);
            case 72 -> entity = new EntityEnderEye(clientWorldController, d0, d1, d2);
            case 76 -> entity = new EntityFireworkRocket(clientWorldController, d0, d1, d2, null);
            case 63 -> {
                entity = new EntityLargeFireball(clientWorldController, d0, d1, d2, (double) packetIn.getSpeedX() / 8000.0D, (double) packetIn.getSpeedY() / 8000.0D, (double) packetIn.getSpeedZ() / 8000.0D);
                packetIn.func_149002_g(0);
            }
            case 64 -> {
                entity = new EntitySmallFireball(clientWorldController, d0, d1, d2, (double) packetIn.getSpeedX() / 8000.0D, (double) packetIn.getSpeedY() / 8000.0D, (double) packetIn.getSpeedZ() / 8000.0D);
                packetIn.func_149002_g(0);
            }
            case 66 -> {
                entity = new EntityWitherSkull(clientWorldController, d0, d1, d2, (double) packetIn.getSpeedX() / 8000.0D, (double) packetIn.getSpeedY() / 8000.0D, (double) packetIn.getSpeedZ() / 8000.0D);
                packetIn.func_149002_g(0);
            }
            case 62 -> entity = new EntityEgg(clientWorldController, d0, d1, d2);
            case 73 -> {
                entity = new EntityPotion(clientWorldController, d0, d1, d2, packetIn.func_149009_m());
                packetIn.func_149002_g(0);
            }
            case 75 -> {
                entity = new EntityExpBottle(clientWorldController, d0, d1, d2);
                packetIn.func_149002_g(0);
            }
            case 1 -> entity = new EntityBoat(clientWorldController, d0, d1, d2);
            case 50 -> entity = new EntityTNTPrimed(clientWorldController, d0, d1, d2, null);
            case 78 -> entity = new EntityArmorStand(clientWorldController, d0, d1, d2);
            case 51 -> entity = new EntityEnderCrystal(clientWorldController, d0, d1, d2);
            case 2 -> entity = new EntityItem(clientWorldController, d0, d1, d2);
            case 70 -> {
                entity = new EntityFallingBlock(clientWorldController, d0, d1, d2, Block.getStateById(packetIn.func_149009_m() & 65535));
                packetIn.func_149002_g(0);
            }
        }

        if (entity != null) {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            entity.rotationPitch = (float) (packetIn.getPitch() * 360) / 256.0F;
            entity.rotationYaw = (float) (packetIn.getYaw() * 360) / 256.0F;
            Entity[] aentity = entity.getParts();

            if (aentity != null) {
                int i = packetIn.getEntityID() - entity.getEntityId();

                for (Entity value : aentity) {
                    value.setEntityId(value.getEntityId() + i);
                }
            }

            entity.setEntityId(packetIn.getEntityID());
            clientWorldController.addEntityToWorld(packetIn.getEntityID(), entity);

            if (packetIn.func_149009_m() > 0) {
                if (packetIn.getType() == 60) {
                    Entity entity2 = clientWorldController.getEntityByID(packetIn.func_149009_m());

                    if (entity2 instanceof EntityLivingBase && entity instanceof EntityArrow) {
                        ((EntityArrow) entity).shootingEntity = entity2;
                    }
                }

                entity.setVelocity((double) packetIn.getSpeedX() / 8000.0D, (double) packetIn.getSpeedY() / 8000.0D, (double) packetIn.getSpeedZ() / 8000.0D);
            }
        }
    }

    /**
     * Spawns an experience orb and sets its value (amount of XP)
     */
    public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = new EntityXPOrb(clientWorldController, (double) packetIn.getX() / 32.0D, (double) packetIn.getY() / 32.0D, (double) packetIn.getZ() / 32.0D, packetIn.getXPValue());
        entity.serverPosX = packetIn.getX();
        entity.serverPosY = packetIn.getY();
        entity.serverPosZ = packetIn.getZ();
        entity.rotationYaw = 0.0F;
        entity.rotationPitch = 0.0F;
        entity.setEntityId(packetIn.getEntityID());
        clientWorldController.addEntityToWorld(packetIn.getEntityID(), entity);
    }

    /**
     * Handles globally visible entities. Used in vanilla for lightning bolts
     */
    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        double d0 = (double) packetIn.func_149051_d() / 32.0D;
        double d1 = (double) packetIn.func_149050_e() / 32.0D;
        double d2 = (double) packetIn.func_149049_f() / 32.0D;
        Entity entity = null;

        if (packetIn.func_149053_g() == 1) {
            entity = new EntityLightningBolt(clientWorldController, d0, d1, d2);
        }

        if (entity != null) {
            entity.serverPosX = packetIn.func_149051_d();
            entity.serverPosY = packetIn.func_149050_e();
            entity.serverPosZ = packetIn.func_149049_f();
            entity.rotationYaw = 0.0F;
            entity.rotationPitch = 0.0F;
            entity.setEntityId(packetIn.func_149052_c());
            clientWorldController.addWeatherEffect(entity);
        }
    }

    /**
     * Handles the spawning of a painting object
     */
    public void handleSpawnPainting(S10PacketSpawnPainting packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPainting entitypainting = new EntityPainting(clientWorldController, packetIn.getPosition(), packetIn.getFacing(), packetIn.getTitle());
        clientWorldController.addEntityToWorld(packetIn.getEntityID(), entitypainting);
    }

    /**
     * Sets the velocity of the specified entity to the specified value
     */
    public void handleEntityVelocity(S12PacketEntityVelocity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityID());
        Minecraft mc = Minecraft.getMinecraft();

        if (entity != null && entity == mc.thePlayer && !mc.thePlayer.capabilities.isFlying) {
            if (mc.gameSettings.velocityhori == 0 && mc.gameSettings.velocityvert == 0) {
                entity.setVelocity(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            } else if (mc.gameSettings.velocityvert == 0) {
                entity.setVelocity((double) packetIn.getMotionX() * (mc.gameSettings.velocityhori / 100D) / 8000.0D, mc.thePlayer.motionY, (double) packetIn.getMotionZ() * (mc.gameSettings.velocityhori / 100D) / 8000.0D);
            } else if (mc.gameSettings.velocityhori == 0) {
                entity.setVelocity(mc.thePlayer.motionX, (double) packetIn.getMotionY() * (mc.gameSettings.velocityvert / 100D) / 8000.0D, mc.thePlayer.motionZ);
            } else {
                entity.setVelocity((double) packetIn.getMotionX() * (mc.gameSettings.velocityhori / 100D) / 8000.0D, (double) packetIn.getMotionY() * (mc.gameSettings.velocityvert / 100D) / 8000.0D, (double) packetIn.getMotionZ() * (mc.gameSettings.velocityhori / 100D) / 8000.0D);
            }
        } else if (entity != null) {
            entity.setVelocity((double) packetIn.getMotionX() / 8000.0D, (double) packetIn.getMotionY() / 8000.0D, (double) packetIn.getMotionZ() / 8000.0D);
        }
    }

    /**
     * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
     * changed -> Registers any changes locally
     */
    public void handleEntityMetadata(S1CPacketEntityMetadata packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null && packetIn.func_149376_c() != null) {
            entity.getDataWatcher().updateWatchedObjectsFromList(packetIn.func_149376_c());
        }
    }

    /**
     * Handles the creation of a nearby player entity, sets the position and held item
     */
    public void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        double d0 = (double) packetIn.getX() / 32.0D;
        double d1 = (double) packetIn.getY() / 32.0D;
        double d2 = (double) packetIn.getZ() / 32.0D;
        float f = (float) (packetIn.getYaw() * 360) / 256.0F;
        float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
        EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(gameController.theWorld, getPlayerInfo(packetIn.getPlayer()).getGameProfile());
        entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = entityotherplayermp.serverPosX = packetIn.getX();
        entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = entityotherplayermp.serverPosY = packetIn.getY();
        entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = entityotherplayermp.serverPosZ = packetIn.getZ();
        int i = packetIn.getCurrentItemID();

        if (i == 0) {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = null;
        } else {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = new ItemStack(Item.getItemById(i), 1, 0);
        }

        entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
        clientWorldController.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
        List<DataWatcher.WatchableObject> list = packetIn.func_148944_c();

        if (list != null) {
            entityotherplayermp.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    /**
     * Updates an entity's position and rotation as specified by the packet
     */
    public void handleEntityTeleport(S18PacketEntityTeleport packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null) {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = (float) (packetIn.getYaw() * 360) / 256.0F;
            float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;

            if (Math.abs(entity.posX - d0) < 0.03125D && Math.abs(entity.posY - d1) < 0.015625D && Math.abs(entity.posZ - d2) < 0.03125D) {
                entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, f1, 3, true);
            } else {
                entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true);
            }

            entity.onGround = packetIn.getOnGround();
        }
    }

    /**
     * Updates which hotbar slot of the player is currently selected
     */
    public void handleHeldItemChange(S09PacketHeldItemChange packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (packetIn.getHeldItemHotbarIndex() >= 0 && packetIn.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
            gameController.thePlayer.inventory.currentItem = packetIn.getHeldItemHotbarIndex();
        }
    }

    /**
     * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
     * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
     * rotation or both).
     */
    public void handleEntityMovement(S14PacketEntity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null) {
            entity.serverPosX += packetIn.func_149062_c();
            entity.serverPosY += packetIn.func_149061_d();
            entity.serverPosZ += packetIn.func_149064_e();
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = packetIn.func_149060_h() ? (float) (packetIn.func_149066_f() * 360) / 256.0F : entity.rotationYaw;
            float f1 = packetIn.func_149060_h() ? (float) (packetIn.func_149063_g() * 360) / 256.0F : entity.rotationPitch;
            entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, false);
            entity.onGround = packetIn.getOnGround();
        }
    }

    /**
     * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
     * rotation of the entity itself
     */
    public void handleEntityHeadLook(S19PacketEntityHeadLook packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null) {
            float f = (float) (packetIn.getYaw() * 360) / 256.0F;
            entity.setRotationYawHead(f);
        }
    }

    /**
     * Locally eliminates the entities. Invoked by the server when the items are in fact destroyed, or the player is no
     * longer registered as required to monitor them. The latter  happens when distance between the player and item
     * increases beyond a certain treshold (typically the viewing distance)
     */
    public void handleDestroyEntities(S13PacketDestroyEntities packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        for (int i = 0; i < packetIn.getEntityIDs().length; ++i) {
            clientWorldController.removeEntityFromWorld(packetIn.getEntityIDs()[i]);
        }
    }

    /**
     * Handles changes in player positioning and rotation such as when travelling to a new dimension, (re)spawning,
     * mounting horses etc. Seems to immediately reply to the server with the clients post-processing perspective on the
     * player positioning
     */
    public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;
        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();
        float f = packetIn.getYaw();
        float f1 = packetIn.getPitch();

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X)) {
            d0 += entityplayer.posX;
        } else {
            entityplayer.motionX = 0.0D;
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
            d1 += entityplayer.posY;
        } else {
            entityplayer.motionY = 0.0D;
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
            d2 += entityplayer.posZ;
        } else {
            entityplayer.motionZ = 0.0D;
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
            f1 += entityplayer.rotationPitch;
        }

        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
            f += entityplayer.rotationYaw;
        }

        entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
        netManager.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch, false));

        if (!doneLoadingTerrain) {
            gameController.thePlayer.prevPosX = gameController.thePlayer.posX;
            gameController.thePlayer.prevPosY = gameController.thePlayer.posY;
            gameController.thePlayer.prevPosZ = gameController.thePlayer.posZ;
            doneLoadingTerrain = true;
            gameController.displayGuiScreen(null);
        }
    }

    /**
     * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
     * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
     * S21PacketChunkData
     */
    public void handleMultiBlockChange(S22PacketMultiBlockChange packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        for (S22PacketMultiBlockChange.BlockUpdateData s22packetmultiblockchange$blockupdatedata : packetIn.getChangedBlocks()) {
            clientWorldController.invalidateRegionAndSetBlock(s22packetmultiblockchange$blockupdatedata.getPos(), s22packetmultiblockchange$blockupdatedata.getBlockState());
        }
    }

    /**
     * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
     */
    public void handleChunkData(S21PacketChunkData packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (packetIn.func_149274_i()) {
            if (packetIn.getExtractedSize() == 0) {
                clientWorldController.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), false);
                return;
            }

            clientWorldController.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), true);
        }

        clientWorldController.invalidateBlockReceiveRegion(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);
        Chunk chunk = clientWorldController.getChunkFromChunkCoords(packetIn.getChunkX(), packetIn.getChunkZ());
        chunk.fillChunk(packetIn.getExtractedDataBytes(), packetIn.getExtractedSize(), packetIn.func_149274_i());
        clientWorldController.markBlockRangeForRenderUpdate(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);

        if (!packetIn.func_149274_i() || !(clientWorldController.provider instanceof WorldProviderSurface)) {
            chunk.resetRelightChecks();
        }
    }

    /**
     * Updates the block and metadata and generates a blockupdate (and notify the clients)
     */
    public void handleBlockChange(S23PacketBlockChange packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        clientWorldController.invalidateRegionAndSetBlock(packetIn.getBlockPosition(), packetIn.getBlockState());
    }

    /**
     * Closes the network channel
     */
    public void handleDisconnect(S40PacketDisconnect packetIn) {
        netManager.closeChannel(packetIn.getReason());
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason) {
        gameController.loadWorld(null);

        if (guiScreenServer != null) {
            if (guiScreenServer instanceof GuiScreenRealmsProxy) {
                gameController.displayGuiScreen((new DisconnectedRealmsScreen(((GuiScreenRealmsProxy) guiScreenServer).func_154321_a(), "disconnect.lost", reason)).getProxy());
            } else {
                gameController.displayGuiScreen(new GuiDisconnected(guiScreenServer, "disconnect.lost", reason));
            }
        } else {
            gameController.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new MainMenu()), "disconnect.lost", reason));
        }
    }

    public void addToSendQueue(Packet p_147297_1_) {
        netManager.sendPacket(p_147297_1_);
    }

    public void handleCollectItem(S0DPacketCollectItem packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getCollectedItemEntityID());
        EntityLivingBase entitylivingbase = (EntityLivingBase) clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entitylivingbase == null) {
            entitylivingbase = gameController.thePlayer;
        }

        if (entity != null) {
            if (entity instanceof EntityXPOrb) {
                clientWorldController.playSoundAtEntity(entity, "random.orb", 0.2F, ((avRandomizer.nextFloat() - avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            } else {
                clientWorldController.playSoundAtEntity(entity, "random.pop", 0.2F, ((avRandomizer.nextFloat() - avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            gameController.effectRenderer.addEffect(new EntityPickupFX(clientWorldController, entity, entitylivingbase, 0.5F));
            clientWorldController.removeEntityFromWorld(packetIn.getCollectedItemEntityID());
        }
    }

    /**
     * Prints a chatmessage in the chat GUI
     */
    public void handleChat(S02PacketChat packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Minecraft mc = Minecraft.getMinecraft();
        String Name = mc.thePlayer.getName();
        String Inputmessage = packetIn.getChatComponent().getFormattedText();
        String ignorefirst = Inputmessage.substring(Inputmessage.indexOf(Name) + Name.length());
        setKeyword();

        if (ignorefirst.contains(Name)) {
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.orb", 1f, 1f, false);
        }

        if (mc.gameSettings.autogg) {
            if (packetIn.getChatComponent().getUnformattedText().endsWith("You earned") || packetIn.getChatComponent().getUnformattedText().endsWith("You won!") || packetIn.getChatComponent().getUnformattedText().endsWith("Reward Summary") || packetIn.getChatComponent().getUnformattedText().endsWith("(Win)") || packetIn.getChatComponent().getUnformattedText().endsWith("(获胜)")) {
                mc.thePlayer.sendChatMessage("/ac gg");
            }
            for (int i = 0; i < wocao.size(); i++) {
                if (Inputmessage.contains((CharSequence) wocao.get(i) + " 原")) {
                    mc.thePlayer.sendChatMessage("原神怎么你了");
                    break;
                }
            }
        }

        if (packetIn.getType() == 2) {
            gameController.ingameGUI.setRecordPlaying(packetIn.getChatComponent(), false);
        } else {
            gameController.ingameGUI.getChatGUI().printChatMessage(packetIn.getChatComponent());
        }

    }

    private static ArrayList wocao = new ArrayList();

    public static void setKeyword() {
        wocao.clear();
        wocao.add("我超");
        wocao.add("我草");
        wocao.add("我操");
        wocao.add("卧槽");
        wocao.add("我艹");
    }

    /**
     * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt
     * or receiving a critical hit by normal or magical means
     */
    public void handleAnimation(S0BPacketAnimation packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entity != null) {
            switch (packetIn.getAnimationType()) {
                case 0 -> {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                    entitylivingbase.swingItem();
                }
                case 1 -> entity.performHurtAnimation();
                case 2 -> {
                    EntityPlayer entityplayer = (EntityPlayer) entity;
                    entityplayer.wakeUpPlayer(false, false, false);
                }
                case 4 -> gameController.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT);
                case 5 -> gameController.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC);
            }
        }
    }

    /**
     * Retrieves the player identified by the packet, puts him to sleep if possible (and flags whether all players are
     * asleep)
     */
    public void handleUseBed(S0APacketUseBed packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        packetIn.getPlayer(clientWorldController).trySleep(packetIn.getBedPosition());
    }

    /**
     * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
     * entities Datawatchers with the entity metadata specified in the packet
     */
    public void handleSpawnMob(S0FPacketSpawnMob packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        double d0 = (double) packetIn.getX() / 32.0D;
        double d1 = (double) packetIn.getY() / 32.0D;
        double d2 = (double) packetIn.getZ() / 32.0D;
        float f = (float) (packetIn.getYaw() * 360) / 256.0F;
        float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
        EntityLivingBase entitylivingbase = (EntityLivingBase) EntityList.createEntityByID(packetIn.getEntityType(), gameController.theWorld);
        entitylivingbase.serverPosX = packetIn.getX();
        entitylivingbase.serverPosY = packetIn.getY();
        entitylivingbase.serverPosZ = packetIn.getZ();
        entitylivingbase.renderYawOffset = entitylivingbase.rotationYawHead = (float) (packetIn.getHeadPitch() * 360) / 256.0F;
        Entity[] aentity = entitylivingbase.getParts();

        if (aentity != null) {
            int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

            for (Entity entity : aentity) {
                entity.setEntityId(entity.getEntityId() + i);
            }
        }

        entitylivingbase.setEntityId(packetIn.getEntityID());
        entitylivingbase.setPositionAndRotation(d0, d1, d2, f, f1);
        entitylivingbase.motionX = (float) packetIn.getVelocityX() / 8000.0F;
        entitylivingbase.motionY = (float) packetIn.getVelocityY() / 8000.0F;
        entitylivingbase.motionZ = (float) packetIn.getVelocityZ() / 8000.0F;
        clientWorldController.addEntityToWorld(packetIn.getEntityID(), entitylivingbase);
        List<DataWatcher.WatchableObject> list = packetIn.func_149027_c();

        if (list != null) {
            entitylivingbase.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    public void handleTimeUpdate(S03PacketTimeUpdate packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.theWorld.setTotalWorldTime(packetIn.getTotalWorldTime());
        gameController.theWorld.setWorldTime(packetIn.getWorldTime());
    }

    public void handleSpawnPosition(S05PacketSpawnPosition packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.thePlayer.setSpawnPoint(packetIn.getSpawnPos(), true);
        gameController.theWorld.getWorldInfo().setSpawn(packetIn.getSpawnPos());
    }

    public void handleEntityAttach(S1BPacketEntityAttach packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());
        Entity entity1 = clientWorldController.getEntityByID(packetIn.getVehicleEntityId());

        if (packetIn.getLeash() == 0) {
            boolean flag = false;

            if (packetIn.getEntityId() == gameController.thePlayer.getEntityId()) {
                entity = gameController.thePlayer;

                if (entity1 instanceof EntityBoat) {
                    ((EntityBoat) entity1).setIsBoatEmpty(false);
                }

                flag = entity.ridingEntity == null && entity1 != null;
            } else if (entity1 instanceof EntityBoat) {
                ((EntityBoat) entity1).setIsBoatEmpty(true);
            }

            if (entity == null) {
                return;
            }

            entity.mountEntity(entity1);

            if (flag) {
                GameSettings gamesettings = gameController.gameSettings;
                gameController.ingameGUI.setRecordPlaying(I18n.format("mount.onboard", GameSettings.getKeyDisplayString(gamesettings.keyBindSneak.getKeyCode())), false);
            }
        } else if (packetIn.getLeash() == 1 && entity instanceof EntityLiving) {
            if (entity1 != null) {
                ((EntityLiving) entity).setLeashedToEntity(entity1, false);
            } else {
                ((EntityLiving) entity).clearLeashed(false, false);
            }
        }
    }

    /**
     * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death),
     * MinecartMobSpawner (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch
     * (spawn particles), Zombie (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke
     * particles), Sheep (...), Tameable (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
     */
    public void handleEntityStatus(S19PacketEntityStatus packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null) {
            if (packetIn.getOpCode() == 21) {
                gameController.getSoundHandler().playSound(new GuardianSound((EntityGuardian) entity));
            } else {
                entity.handleStatusUpdate(packetIn.getOpCode());
            }
        }
    }

    public void handleUpdateHealth(S06PacketUpdateHealth packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.thePlayer.setPlayerSPHealth(packetIn.getHealth());
        gameController.thePlayer.getFoodStats().setFoodLevel(packetIn.getFoodLevel());
        gameController.thePlayer.getFoodStats().setFoodSaturationLevel(packetIn.getSaturationLevel());
    }

    public void handleSetExperience(S1FPacketSetExperience packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.thePlayer.setXPStats(packetIn.func_149397_c(), packetIn.getTotalExperience(), packetIn.getLevel());
    }

    public void handleRespawn(S07PacketRespawn packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (packetIn.getDimensionID() != gameController.thePlayer.dimension) {
            doneLoadingTerrain = false;
            Scoreboard scoreboard = clientWorldController.getScoreboard();
            clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, gameController.theWorld.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), gameController.mcProfiler);
            clientWorldController.setWorldScoreboard(scoreboard);
            gameController.loadWorld(clientWorldController);
            gameController.thePlayer.dimension = packetIn.getDimensionID();
//            gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        }

        gameController.setDimensionAndSpawnPlayer(packetIn.getDimensionID());
        gameController.playerController.setGameType(packetIn.getGameType());
    }

    /**
     * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
     */
    public void handleExplosion(S27PacketExplosion packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Explosion explosion = new Explosion(gameController.theWorld, null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        explosion.doExplosionB(true);
        gameController.thePlayer.motionX += packetIn.func_149149_c();
        gameController.thePlayer.motionY += packetIn.func_149144_d();
        gameController.thePlayer.motionZ += packetIn.func_149147_e();
    }

    /**
     * Displays a GUI by ID. In order starting from id 0: Chest, Workbench, Furnace, Dispenser, Enchanting table,
     * Brewing stand, Villager merchant, Beacon, Anvil, Hopper, Dropper, Horse
     */
    public void handleOpenWindow(S2DPacketOpenWindow packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayerSP entityplayersp = gameController.thePlayer;

        if ("minecraft:container".equals(packetIn.getGuiId())) {
            entityplayersp.displayGUIChest(new InventoryBasic(packetIn.getWindowTitle(), packetIn.getSlotCount()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        } else if ("minecraft:villager".equals(packetIn.getGuiId())) {
            entityplayersp.displayVillagerTradeGui(new NpcMerchant(entityplayersp, packetIn.getWindowTitle()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        } else if ("EntityHorse".equals(packetIn.getGuiId())) {
            Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

            if (entity instanceof EntityHorse) {
                entityplayersp.displayGUIHorse((EntityHorse) entity, new AnimalChest(packetIn.getWindowTitle(), packetIn.getSlotCount()));
                entityplayersp.openContainer.windowId = packetIn.getWindowId();
            }
        } else if (!packetIn.hasSlots()) {
            entityplayersp.displayGui(new LocalBlockIntercommunication(packetIn.getGuiId(), packetIn.getWindowTitle()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        } else {
            ContainerLocalMenu containerlocalmenu = new ContainerLocalMenu(packetIn.getGuiId(), packetIn.getWindowTitle(), packetIn.getSlotCount());
            entityplayersp.displayGUIChest(containerlocalmenu);
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        }
    }

    /**
     * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
     */
    public void handleSetSlot(S2FPacketSetSlot packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;

        if (packetIn.func_149175_c() == -1) {
            entityplayer.inventory.setItemStack(packetIn.func_149174_e());
        } else {
            boolean flag = false;

            if (gameController.currentScreen instanceof GuiContainerCreative guicontainercreative) {
                flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (packetIn.func_149175_c() == 0 && packetIn.func_149173_d() >= 36 && packetIn.func_149173_d() < 45) {
                ItemStack itemstack = entityplayer.inventoryContainer.getSlot(packetIn.func_149173_d()).getStack();

                if (packetIn.func_149174_e() != null && (itemstack == null || itemstack.stackSize < packetIn.func_149174_e().stackSize)) {
                    packetIn.func_149174_e().animationsToGo = 5;
                }

                entityplayer.inventoryContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
            } else if (packetIn.func_149175_c() == entityplayer.openContainer.windowId && (packetIn.func_149175_c() != 0 || !flag)) {
                entityplayer.openContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
            }
        }
    }

    /**
     * Verifies that the server and client are synchronized with respect to the inventory/container opened by the player
     * and confirms if it is the case.
     */
    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Container container = null;
        EntityPlayer entityplayer = gameController.thePlayer;

        if (packetIn.getWindowId() == 0) {
            container = entityplayer.inventoryContainer;
        } else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
            container = entityplayer.openContainer;
        }

        if (container != null && !packetIn.func_148888_e()) {
            addToSendQueue(new C0FPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
        }
    }

    /**
     * Handles the placement of a specified ItemStack in a specified container/inventory slot
     */
    public void handleWindowItems(S30PacketWindowItems packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;

        if (packetIn.func_148911_c() == 0) {
            entityplayer.inventoryContainer.putStacksInSlots(packetIn.getItemStacks());
        } else if (packetIn.func_148911_c() == entityplayer.openContainer.windowId) {
            entityplayer.openContainer.putStacksInSlots(packetIn.getItemStacks());
        }
    }

    /**
     * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
     */
    public void handleSignEditorOpen(S36PacketSignEditorOpen packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        TileEntity tileentity = clientWorldController.getTileEntity(packetIn.getSignPosition());

        if (!(tileentity instanceof TileEntitySign)) {
            tileentity = new TileEntitySign();
            tileentity.setWorldObj(clientWorldController);
            tileentity.setPos(packetIn.getSignPosition());
        }

        gameController.thePlayer.openEditSign((TileEntitySign) tileentity);
    }

    /**
     * Updates a specified sign with the specified text lines
     */
    public void handleUpdateSign(S33PacketUpdateSign packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        boolean flag = false;

        if (gameController.theWorld.isBlockLoaded(packetIn.getPos())) {
            TileEntity tileentity = gameController.theWorld.getTileEntity(packetIn.getPos());

            if (tileentity instanceof TileEntitySign tileentitysign) {

                if (tileentitysign.getIsEditable()) {
                    System.arraycopy(packetIn.getLines(), 0, tileentitysign.signText, 0, 4);
                    tileentitysign.markDirty();
                }

                flag = true;
            }
        }

        if (!flag && gameController.thePlayer != null) {
            gameController.thePlayer.addChatMessage(new ChatComponentText("Unable to locate sign at " + packetIn.getPos().getX() + ", " + packetIn.getPos().getY() + ", " + packetIn.getPos().getZ()));
        }
    }

    /**
     * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
     * beacons, skulls, flowerpot
     */
    public void handleUpdateTileEntity(S35PacketUpdateTileEntity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (gameController.theWorld.isBlockLoaded(packetIn.getPos())) {
            TileEntity tileentity = gameController.theWorld.getTileEntity(packetIn.getPos());
            int i = packetIn.getTileEntityType();

            if (i == 1 && tileentity instanceof TileEntityMobSpawner || i == 2 && tileentity instanceof TileEntityCommandBlock || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner) {
                tileentity.readFromNBT(packetIn.getNbtCompound());
            }
        }
    }

    /**
     * Sets the progressbar of the opened window to the specified value
     */
    public void handleWindowProperty(S31PacketWindowProperty packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;

        if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == packetIn.getWindowId()) {
            entityplayer.openContainer.updateProgressBar(packetIn.getVarIndex(), packetIn.getVarValue());
        }
    }

    public void handleEntityEquipment(S04PacketEntityEquipment packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entity != null) {
            entity.setCurrentItemOrArmor(packetIn.getEquipmentSlot(), packetIn.getItemStack());
        }
    }

    /**
     * Resets the ItemStack held in hand and closes the window that is opened
     */
    public void handleCloseWindow(S2EPacketCloseWindow packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.thePlayer.closeScreenAndDropStack();
    }

    /**
     * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
     * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
     * accessing a (Ender)Chest
     */
    public void handleBlockAction(S24PacketBlockAction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.theWorld.addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
    }

    /**
     * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
     */
    public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.theWorld.sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
    }

    public void handleMapChunkBulk(S26PacketMapChunkBulk packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        for (int i = 0; i < packetIn.getChunkCount(); ++i) {
            int j = packetIn.getChunkX(i);
            int k = packetIn.getChunkZ(i);
            clientWorldController.doPreChunk(j, k, true);
            clientWorldController.invalidateBlockReceiveRegion(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);
            Chunk chunk = clientWorldController.getChunkFromChunkCoords(j, k);
            chunk.fillChunk(packetIn.getChunkBytes(i), packetIn.getChunkSize(i), true);
            clientWorldController.markBlockRangeForRenderUpdate(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);

            if (!(clientWorldController.provider instanceof WorldProviderSurface)) {
                chunk.resetRelightChecks();
            }
        }
    }

    public void handleChangeGameState(S2BPacketChangeGameState packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;
        int i = packetIn.getGameState();
        float f = packetIn.func_149137_d();
        int j = MathHelper.floor_float(f + 0.5F);

        if (i >= 0 && i < S2BPacketChangeGameState.MESSAGE_NAMES.length && S2BPacketChangeGameState.MESSAGE_NAMES[i] != null) {
            entityplayer.addChatComponentMessage(new ChatComponentTranslation(S2BPacketChangeGameState.MESSAGE_NAMES[i]));
        }

        switch (i) {
            case 1 -> {
                clientWorldController.getWorldInfo().setRaining(true);
                clientWorldController.setRainStrength(0.0F);
            }
            case 2 -> {
                clientWorldController.getWorldInfo().setRaining(false);
                clientWorldController.setRainStrength(1.0F);
            }
            case 3 -> gameController.playerController.setGameType(WorldSettings.GameType.getByID(j));
            case 4 -> gameController.displayGuiScreen(new GuiWinGame());
            case 5 -> {
                GameSettings gamesettings = gameController.gameSettings;
                if (f == 0.0F) {
                    gameController.displayGuiScreen(new GuiScreenDemo());
                } else if (f == 101.0F) {
                    gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.movement", GameSettings.getKeyDisplayString(gamesettings.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindRight.getKeyCode())));
                } else if (f == 102.0F) {
                    gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.jump", GameSettings.getKeyDisplayString(gamesettings.keyBindJump.getKeyCode())));
                } else if (f == 103.0F) {
                    gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.inventory", GameSettings.getKeyDisplayString(gamesettings.keyBindInventory.getKeyCode())));
                }
            }
            case 6 ->
                    clientWorldController.playSound(entityplayer.posX, entityplayer.posY + (double) entityplayer.getEyeHeight(), entityplayer.posZ, "random.successful_hit", 0.18F, 0.45F, false);
            case 7 -> clientWorldController.setRainStrength(f);
            case 8 -> clientWorldController.setThunderStrength(f);
            case 10 -> {
                clientWorldController.spawnParticle(EnumParticleTypes.MOB_APPEARANCE, entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
                clientWorldController.playSound(entityplayer.posX, entityplayer.posY, entityplayer.posZ, "mob.guardian.curse", 1.0F, 1.0F, false);
            }
        }
    }

    /**
     * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
     * MapItemRenderer for it
     */
    public void handleMaps(S34PacketMaps packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(), gameController.theWorld);
        packetIn.setMapdataTo(mapdata);
        gameController.entityRenderer.getMapItemRenderer().updateMapTexture(mapdata);
    }

    public void handleEffect(S28PacketEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (packetIn.isSoundServerwide()) {
            gameController.theWorld.playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        } else {
            gameController.theWorld.playAuxSFX(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
    }

    /**
     * Updates the players statistics or achievements
     */
    public void handleStatistics(S37PacketStatistics packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        boolean flag = false;

        for (Entry<StatBase, Integer> entry : packetIn.func_148974_c().entrySet()) {
            StatBase statbase = entry.getKey();
            int i = entry.getValue().intValue();

            if (statbase.isAchievement() && i > 0) {
                if (field_147308_k && gameController.thePlayer.getStatFileWriter().readStat(statbase) == 0) {
                    Achievement achievement = (Achievement) statbase;
                    gameController.guiAchievement.displayAchievement(achievement);

                    if (statbase == AchievementList.openInventory) {
                        gameController.gameSettings.showInventoryAchievementHint = false;
                        gameController.gameSettings.saveOptions();
                    }
                }

                flag = true;
            }

            gameController.thePlayer.getStatFileWriter().unlockAchievement(gameController.thePlayer, statbase, i);
        }

        if (!field_147308_k && !flag && gameController.gameSettings.showInventoryAchievementHint) {
            gameController.guiAchievement.displayUnformattedAchievement(AchievementList.openInventory);
        }

        field_147308_k = true;

        if (gameController.currentScreen instanceof IProgressMeter) {
            ((IProgressMeter) gameController.currentScreen).doneLoading();
        }
    }

    public void handleEntityEffect(S1DPacketEntityEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase) {
            PotionEffect potioneffect = new PotionEffect(packetIn.getEffectId(), packetIn.getDuration(), packetIn.getAmplifier(), false, packetIn.func_179707_f());
            potioneffect.setPotionDurationMax(packetIn.func_149429_c());
            ((EntityLivingBase) entity).addPotionEffect(potioneffect);
        }
    }

    public void handleCombatEvent(S42PacketCombatEvent packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.field_179775_c);
        EntityLivingBase entitylivingbase = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;

        if (packetIn.eventType == S42PacketCombatEvent.Event.END_COMBAT) {
            long i = 1000 * packetIn.field_179772_d / 20;
            MetadataCombat metadatacombat = new MetadataCombat(gameController.thePlayer, entitylivingbase);
        } else if (packetIn.eventType == S42PacketCombatEvent.Event.ENTITY_DIED) {
            Entity entity1 = clientWorldController.getEntityByID(packetIn.field_179774_b);

            if (entity1 instanceof EntityPlayer) {
                MetadataPlayerDeath metadataplayerdeath = new MetadataPlayerDeath((EntityPlayer) entity1, entitylivingbase);
                metadataplayerdeath.func_152807_a(packetIn.deathMessage);
            }
        }
    }

    public void handleServerDifficulty(S41PacketServerDifficulty packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.theWorld.getWorldInfo().setDifficulty(packetIn.getDifficulty());
        gameController.theWorld.getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
    }

    public void handleCamera(S43PacketCamera packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null) {
            gameController.setRenderViewEntity(entity);
        }
    }

    public void handleWorldBorder(S44PacketWorldBorder packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        packetIn.func_179788_a(clientWorldController.getWorldBorder());
    }

    @SuppressWarnings("incomplete-switch")
    public void handleTitle(S45PacketTitle packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        S45PacketTitle.Type s45packettitle$type = packetIn.getType();
        String s = null;
        String s1 = null;
        String s2 = packetIn.getMessage() != null ? packetIn.getMessage().getFormattedText() : "";

        switch (s45packettitle$type) {
            case TITLE -> s = s2;
            case SUBTITLE -> s1 = s2;
            case RESET -> {
                gameController.ingameGUI.displayTitle("", "", -1, -1, -1);
                gameController.ingameGUI.setDefaultTitlesTimes();
                return;
            }
        }

        gameController.ingameGUI.displayTitle(s, s1, packetIn.getFadeInTime(), packetIn.getDisplayTime(), packetIn.getFadeOutTime());
    }

    public void handleSetCompressionLevel(S46PacketSetCompressionLevel packetIn) {
        if (!netManager.isLocalChannel()) {
            netManager.setCompressionTreshold(packetIn.getThreshold());
        }
    }

    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn) {
        gameController.ingameGUI.getTabList().setHeader(packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader());
        gameController.ingameGUI.getTabList().setFooter(packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter());
    }

    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).removePotionEffectClient(packetIn.getEffectId());
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void handlePlayerListItem(S38PacketPlayerListItem packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        for (S38PacketPlayerListItem.AddPlayerData s38packetplayerlistitem$addplayerdata : packetIn.getEntries()) {
            if (packetIn.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                playerInfoMap.remove(s38packetplayerlistitem$addplayerdata.getProfile().getId());
            } else {
                NetworkPlayerInfo networkplayerinfo = playerInfoMap.get(s38packetplayerlistitem$addplayerdata.getProfile().getId());

                if (packetIn.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                    networkplayerinfo = new NetworkPlayerInfo(s38packetplayerlistitem$addplayerdata);
                    playerInfoMap.put(networkplayerinfo.getGameProfile().getId(), networkplayerinfo);
                }

                if (networkplayerinfo != null) {
                    switch (packetIn.getAction()) {
                        case ADD_PLAYER -> {
                            networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
                            networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
                        }
                        case UPDATE_GAME_MODE ->
                                networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
                        case UPDATE_LATENCY ->
                                networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
                        case UPDATE_DISPLAY_NAME ->
                                networkplayerinfo.setDisplayName(s38packetplayerlistitem$addplayerdata.getDisplayName());
                    }
                }
            }
        }
    }

    public void handleKeepAlive(S00PacketKeepAlive packetIn) {
        addToSendQueue(new C00PacketKeepAlive(packetIn.func_149134_c()));
    }

    public void handlePlayerAbilities(S39PacketPlayerAbilities packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        EntityPlayer entityplayer = gameController.thePlayer;
        entityplayer.capabilities.isFlying = packetIn.isFlying();
        entityplayer.capabilities.isCreativeMode = packetIn.isCreativeMode();
        entityplayer.capabilities.disableDamage = packetIn.isInvulnerable();
        entityplayer.capabilities.allowFlying = packetIn.isAllowFlying();
        entityplayer.capabilities.setFlySpeed(packetIn.getFlySpeed());
        entityplayer.capabilities.setPlayerWalkSpeed(packetIn.getWalkSpeed());
    }

    /**
     * Displays the available command-completion options the server knows of
     */
    public void handleTabComplete(S3APacketTabComplete packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        String[] astring = packetIn.func_149630_c();

        if (gameController.currentScreen instanceof GuiChat guichat) {
            guichat.onAutocompleteResponse(astring);
        }
    }

    public void handleSoundEffect(S29PacketSoundEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        gameController.theWorld.playSound(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSoundName(), packetIn.getVolume(), packetIn.getPitch(), false);
    }

    public void handleResourcePack(S48PacketResourcePackSend packetIn) {
        final String s = packetIn.getURL();
        final String s1 = packetIn.getHash();

        if (s.startsWith("level://")) {
            String s2 = s.substring("level://".length());
            File file1 = new File(gameController.mcDataDir, "saves");
            File file2 = new File(file1, s2);

            if (file2.isFile()) {
                netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(gameController.getResourcePackRepository().setResourcePackInstance(file2), new FutureCallback<Object>() {
                    public void onSuccess(Object p_onSuccess_1_) {
                        netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }

                    public void onFailure(Throwable p_onFailure_1_) {
                        netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                });
            } else {
                netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            }
        } else {
            if (gameController.getCurrentServerData() != null && gameController.getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
                netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(gameController.getResourcePackRepository().downloadResourcePack(s, s1), new FutureCallback<Object>() {
                    public void onSuccess(Object p_onSuccess_1_) {
                        netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }

                    public void onFailure(Throwable p_onFailure_1_) {
                        netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                });
            } else if (gameController.getCurrentServerData() != null && gameController.getCurrentServerData().getResourceMode() != ServerData.ServerResourceMode.PROMPT) {
                netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.DECLINED));
            } else {
                gameController.addScheduledTask(() -> {
                    gameController.displayGuiScreen(new GuiYesNo((result, id) -> {
                        gameController = Minecraft.getMinecraft();

                        if (result) {
                            if (gameController.getCurrentServerData() != null) {
                                gameController.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.ENABLED);
                            }

                            netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                            Futures.addCallback(gameController.getResourcePackRepository().downloadResourcePack(s, s1), new FutureCallback<Object>() {
                                public void onSuccess(Object p_onSuccess_1_) {
                                    netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                                }

                                public void onFailure(Throwable p_onFailure_1_) {
                                    netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                                }
                            });
                        } else {
                            if (gameController.getCurrentServerData() != null) {
                                gameController.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.DISABLED);
                            }

                            netManager.sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.DECLINED));
                        }

                        ServerList.func_147414_b(gameController.getCurrentServerData());
                        gameController.displayGuiScreen(null);
                    }, I18n.format("multiplayer.texturePrompt.line1"), I18n.format("multiplayer.texturePrompt.line2"), 0));
                });
            }
        }
    }

    public void handleEntityNBT(S49PacketUpdateEntityNBT packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null) {
            entity.clientUpdateEntityNBT(packetIn.getTagCompound());
        }
    }

    /**
     * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to
     * acquire a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the
     * player instance and finally "MC|RPack" which the server uses to communicate the identifier of the default server
     * resourcepack for the client to load.
     */
    public void handleCustomPayload(S3FPacketCustomPayload packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if ("MC|TrList".equals(packetIn.getChannelName())) {
            PacketBuffer packetbuffer = packetIn.getBufferData();

            try {
                int i = packetbuffer.readInt();
                GuiScreen guiscreen = gameController.currentScreen;

                if (guiscreen != null && guiscreen instanceof GuiMerchant && i == gameController.thePlayer.openContainer.windowId) {
                    IMerchant imerchant = ((GuiMerchant) guiscreen).getMerchant();
                    MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(packetbuffer);
                    imerchant.setRecipes(merchantrecipelist);
                }
            } catch (IOException ioexception) {
                logger.error("Couldn't load trade info", ioexception);
            } finally {
                packetbuffer.release();
            }
        } else if ("MC|Brand".equals(packetIn.getChannelName())) {
            gameController.thePlayer.setClientBrand(packetIn.getBufferData().readStringFromBuffer(32767));
        } else if ("MC|BOpen".equals(packetIn.getChannelName())) {
            ItemStack itemstack = gameController.thePlayer.getCurrentEquippedItem();

            if (itemstack != null && itemstack.getItem() == Items.written_book) {
                gameController.displayGuiScreen(new GuiScreenBook(gameController.thePlayer, itemstack, false));
            }
        }
    }

    /**
     * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
     */
    public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Scoreboard scoreboard = clientWorldController.getScoreboard();

        if (packetIn.func_149338_e() == 0) {
            ScoreObjective scoreobjective = scoreboard.addScoreObjective(packetIn.func_149339_c(), IScoreObjectiveCriteria.DUMMY);
            scoreobjective.setDisplayName(packetIn.func_149337_d());
            scoreobjective.setRenderType(packetIn.func_179817_d());
        } else {
            ScoreObjective scoreobjective1 = scoreboard.getObjective(packetIn.func_149339_c());

            if (packetIn.func_149338_e() == 1) {
                scoreboard.removeObjective(scoreobjective1);
            } else if (packetIn.func_149338_e() == 2) {
                scoreobjective1.setDisplayName(packetIn.func_149337_d());
                scoreobjective1.setRenderType(packetIn.func_179817_d());
            }
        }
    }

    /**
     * Either updates the score with a specified value or removes the score for an objective
     */
    public void handleUpdateScore(S3CPacketUpdateScore packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Scoreboard scoreboard = clientWorldController.getScoreboard();
        ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getObjectiveName());

        if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.CHANGE) {
            Score score = scoreboard.getValueFromObjective(packetIn.getPlayerName(), scoreobjective);
            score.setScorePoints(packetIn.getScoreValue());
        } else if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.REMOVE) {
            if (StringUtils.isNullOrEmpty(packetIn.getObjectiveName())) {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), null);
            } else if (scoreobjective != null) {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), scoreobjective);
            }
        }
    }

    /**
     * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below
     * name)
     */
    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Scoreboard scoreboard = clientWorldController.getScoreboard();

        if (packetIn.func_149370_d().length() == 0) {
            scoreboard.setObjectiveInDisplaySlot(packetIn.func_149371_c(), null);
        } else {
            ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.func_149370_d());
            scoreboard.setObjectiveInDisplaySlot(packetIn.func_149371_c(), scoreobjective);
        }
    }

    /**
     * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
     * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
     */
    public void handleTeams(S3EPacketTeams packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Scoreboard scoreboard = clientWorldController.getScoreboard();
        ScorePlayerTeam scoreplayerteam;

        if (packetIn.getAction() == 0) {
            scoreplayerteam = scoreboard.createTeam(packetIn.getName());
        } else {
            scoreplayerteam = scoreboard.getTeam(packetIn.getName());
        }

        if (packetIn.getAction() == 0 || packetIn.getAction() == 2) {
            scoreplayerteam.setTeamName(packetIn.getDisplayName());
            scoreplayerteam.setNamePrefix(packetIn.getPrefix());
            scoreplayerteam.setNameSuffix(packetIn.getSuffix());
            scoreplayerteam.setChatFormat(EnumChatFormatting.func_175744_a(packetIn.getColor()));
            scoreplayerteam.func_98298_a(packetIn.getFriendlyFlags());
            Team.EnumVisible team$enumvisible = Team.EnumVisible.func_178824_a(packetIn.getNameTagVisibility());

            if (team$enumvisible != null) {
                scoreplayerteam.setNameTagVisibility(team$enumvisible);
            }
        }

        if (packetIn.getAction() == 0 || packetIn.getAction() == 3) {
            for (String s : packetIn.getPlayers()) {
                scoreboard.addPlayerToTeam(s, packetIn.getName());
            }
        }

        if (packetIn.getAction() == 4) {
            for (String s1 : packetIn.getPlayers()) {
                scoreboard.removePlayerFromTeam(s1, scoreplayerteam);
            }
        }

        if (packetIn.getAction() == 1) {
            scoreboard.removeTeam(scoreplayerteam);
        }
    }

    /**
     * Spawns a specified number of particles at the specified location with a randomized displacement according to
     * specified bounds
     */
    public void handleParticles(S2APacketParticles packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);

        if (packetIn.getParticleCount() == 0) {
            double d0 = packetIn.getParticleSpeed() * packetIn.getXOffset();
            double d2 = packetIn.getParticleSpeed() * packetIn.getYOffset();
            double d4 = packetIn.getParticleSpeed() * packetIn.getZOffset();

            try {
                clientWorldController.spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate(), packetIn.getYCoordinate(), packetIn.getZCoordinate(), d0, d2, d4, packetIn.getParticleArgs());
            } catch (Throwable var17) {
                logger.warn("Could not spawn particle effect " + packetIn.getParticleType());
            }
        } else {
            for (int i = 0; i < packetIn.getParticleCount(); ++i) {
                double d1 = avRandomizer.nextGaussian() * (double) packetIn.getXOffset();
                double d3 = avRandomizer.nextGaussian() * (double) packetIn.getYOffset();
                double d5 = avRandomizer.nextGaussian() * (double) packetIn.getZOffset();
                double d6 = avRandomizer.nextGaussian() * (double) packetIn.getParticleSpeed();
                double d7 = avRandomizer.nextGaussian() * (double) packetIn.getParticleSpeed();
                double d8 = avRandomizer.nextGaussian() * (double) packetIn.getParticleSpeed();

                try {
                    clientWorldController.spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate() + d1, packetIn.getYCoordinate() + d3, packetIn.getZCoordinate() + d5, d6, d7, d8, packetIn.getParticleArgs());
                } catch (Throwable var16) {
                    logger.warn("Could not spawn particle effect " + packetIn.getParticleType());
                    return;
                }
            }
        }
    }

    /**
     * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player
     * sprinting, animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie
     * maxHealth and knockback resistance as well as reinforcement spawning chance.
     */
    public void handleEntityProperties(S20PacketEntityProperties packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, gameController);
        Entity entity = clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null) {
            if (!(entity instanceof EntityLivingBase)) {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            } else {
                BaseAttributeMap baseattributemap = ((EntityLivingBase) entity).getAttributeMap();

                for (S20PacketEntityProperties.Snapshot s20packetentityproperties$snapshot : packetIn.func_149441_d()) {
                    IAttributeInstance iattributeinstance = baseattributemap.getAttributeInstanceByName(s20packetentityproperties$snapshot.func_151409_a());

                    if (iattributeinstance == null) {
                        iattributeinstance = baseattributemap.registerAttribute(new RangedAttribute(null, s20packetentityproperties$snapshot.func_151409_a(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
                    }

                    iattributeinstance.setBaseValue(s20packetentityproperties$snapshot.func_151410_b());
                    iattributeinstance.removeAllModifiers();

                    for (AttributeModifier attributemodifier : s20packetentityproperties$snapshot.func_151408_c()) {
                        iattributeinstance.applyModifier(attributemodifier);
                    }
                }
            }
        }
    }

    /**
     * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
     */
    public NetworkManager getNetworkManager() {
        return netManager;
    }

    public Collection<NetworkPlayerInfo> getPlayerInfoMap() {
        return playerInfoMap.values();
    }

    public NetworkPlayerInfo getPlayerInfo(UUID p_175102_1_) {
        return playerInfoMap.get(p_175102_1_);
    }

    /**
     * Gets the client's description information about another player on the server.
     */
    public NetworkPlayerInfo getPlayerInfo(String p_175104_1_) {
        for (NetworkPlayerInfo networkplayerinfo : playerInfoMap.values()) {
            if (networkplayerinfo.getGameProfile().getName().equals(p_175104_1_)) {
                return networkplayerinfo;
            }
        }

        return null;
    }

    public GameProfile getGameProfile() {
        return profile;
    }
}
