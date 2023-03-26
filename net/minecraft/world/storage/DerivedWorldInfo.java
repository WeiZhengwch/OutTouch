package net.minecraft.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class DerivedWorldInfo extends WorldInfo {
    /**
     * Instance of WorldInfo.
     */
    private final WorldInfo theWorldInfo;

    public DerivedWorldInfo(WorldInfo p_i2145_1_) {
        theWorldInfo = p_i2145_1_;
    }

    /**
     * Gets the NBTTagCompound for the worldInfo
     */
    public NBTTagCompound getNBTTagCompound() {
        return theWorldInfo.getNBTTagCompound();
    }

    /**
     * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
     */
    public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {
        return theWorldInfo.cloneNBTCompound(nbt);
    }

    /**
     * Returns the seed of current world.
     */
    public long getSeed() {
        return theWorldInfo.getSeed();
    }

    /**
     * Returns the x spawn position
     */
    public int getSpawnX() {
        return theWorldInfo.getSpawnX();
    }

    /**
     * Set the x spawn position to the passed in value
     */
    public void setSpawnX(int x) {
    }

    /**
     * Return the Y axis spawning point of the player.
     */
    public int getSpawnY() {
        return theWorldInfo.getSpawnY();
    }

    /**
     * Sets the y spawn position
     */
    public void setSpawnY(int y) {
    }

    /**
     * Returns the z spawn position
     */
    public int getSpawnZ() {
        return theWorldInfo.getSpawnZ();
    }

    /**
     * Set the z spawn position to the passed in value
     */
    public void setSpawnZ(int z) {
    }

    public long getWorldTotalTime() {
        return theWorldInfo.getWorldTotalTime();
    }

    public void setWorldTotalTime(long time) {
    }

    /**
     * Get current world time
     */
    public long getWorldTime() {
        return theWorldInfo.getWorldTime();
    }

    /**
     * Set current world time
     */
    public void setWorldTime(long time) {
    }

    public long getSizeOnDisk() {
        return theWorldInfo.getSizeOnDisk();
    }

    /**
     * Returns the player's NBTTagCompound to be loaded
     */
    public NBTTagCompound getPlayerNBTTagCompound() {
        return theWorldInfo.getPlayerNBTTagCompound();
    }

    /**
     * Get current world name
     */
    public String getWorldName() {
        return theWorldInfo.getWorldName();
    }

    public void setWorldName(String worldName) {
    }

    /**
     * Returns the save version of this world
     */
    public int getSaveVersion() {
        return theWorldInfo.getSaveVersion();
    }

    /**
     * Sets the save version of the world
     */
    public void setSaveVersion(int version) {
    }

    /**
     * Return the last time the player was in this world.
     */
    public long getLastTimePlayed() {
        return theWorldInfo.getLastTimePlayed();
    }

    /**
     * Returns true if it is thundering, false otherwise.
     */
    public boolean isThundering() {
        return theWorldInfo.isThundering();
    }

    /**
     * Sets whether it is thundering or not.
     */
    public void setThundering(boolean thunderingIn) {
    }

    /**
     * Returns the number of ticks until next thunderbolt.
     */
    public int getThunderTime() {
        return theWorldInfo.getThunderTime();
    }

    /**
     * Defines the number of ticks until next thunderbolt.
     */
    public void setThunderTime(int time) {
    }

    /**
     * Returns true if it is raining, false otherwise.
     */
    public boolean isRaining() {
        return theWorldInfo.isRaining();
    }

    /**
     * Sets whether it is raining or not.
     */
    public void setRaining(boolean isRaining) {
    }

    /**
     * Return the number of ticks until rain.
     */
    public int getRainTime() {
        return theWorldInfo.getRainTime();
    }

    /**
     * Sets the number of ticks until rain.
     */
    public void setRainTime(int time) {
    }

    /**
     * Gets the GameType.
     */
    public WorldSettings.GameType getGameType() {
        return theWorldInfo.getGameType();
    }

    public void setSpawn(BlockPos spawnPoint) {
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled() {
        return theWorldInfo.isMapFeaturesEnabled();
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean isHardcoreModeEnabled() {
        return theWorldInfo.isHardcoreModeEnabled();
    }

    public WorldType getTerrainType() {
        return theWorldInfo.getTerrainType();
    }

    public void setTerrainType(WorldType type) {
    }

    /**
     * Returns true if commands are allowed on this World.
     */
    public boolean areCommandsAllowed() {
        return theWorldInfo.areCommandsAllowed();
    }

    public void setAllowCommands(boolean allow) {
    }

    /**
     * Returns true if the World is initialized.
     */
    public boolean isInitialized() {
        return theWorldInfo.isInitialized();
    }

    /**
     * Sets the initialization status of the World.
     */
    public void setServerInitialized(boolean initializedIn) {
    }

    /**
     * Gets the GameRules class Instance.
     */
    public GameRules getGameRulesInstance() {
        return theWorldInfo.getGameRulesInstance();
    }

    public EnumDifficulty getDifficulty() {
        return theWorldInfo.getDifficulty();
    }

    public void setDifficulty(EnumDifficulty newDifficulty) {
    }

    public boolean isDifficultyLocked() {
        return theWorldInfo.isDifficultyLocked();
    }

    public void setDifficultyLocked(boolean locked) {
    }
}
