package net.minecraft.client.multiplayer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ServerData {
    public String serverName;
    public String serverIP;

    /**
     * the string indicating number of players on and capacity of the server that is shown on the server browser (i.e.
     * "5/20" meaning 5 slots used out of 20 slots total)
     */
    public String populationInfo;

    /**
     * (better variable name would be 'hostname') server name as displayed in the server browser's second line (grey
     * text)
     */
    public String serverMOTD;

    /**
     * last server ping that showed up in the server browser
     */
    public long pingToServer;
    public int version = 47;

    /**
     * Game version for this server.
     */
    public String gameVersion = "1.8.9";
    public boolean field_78841_f;
    public String playerList;
    private ServerData.ServerResourceMode resourceMode = ServerData.ServerResourceMode.PROMPT;
    private String serverIcon;

    /**
     * True if the server is a LAN server
     */
    private boolean lanServer;

    public ServerData(String name, String ip, boolean isLan) {
        serverName = name;
        serverIP = ip;
        lanServer = isLan;
    }

    /**
     * Takes an NBTTagCompound with 'name' and 'ip' keys, returns a ServerData instance.
     */
    public static ServerData getServerDataFromNBTCompound(NBTTagCompound nbtCompound) {
        ServerData serverdata = new ServerData(nbtCompound.getString("name"), nbtCompound.getString("ip"), false);

        if (nbtCompound.hasKey("icon", 8)) {
            serverdata.setBase64EncodedIconData(nbtCompound.getString("icon"));
        }

        if (nbtCompound.hasKey("acceptTextures", 1)) {
            if (nbtCompound.getBoolean("acceptTextures")) {
                serverdata.setResourceMode(ServerData.ServerResourceMode.ENABLED);
            } else {
                serverdata.setResourceMode(ServerData.ServerResourceMode.DISABLED);
            }
        } else {
            serverdata.setResourceMode(ServerData.ServerResourceMode.PROMPT);
        }

        return serverdata;
    }

    /**
     * Returns an NBTTagCompound with the server's name, IP and maybe acceptTextures.
     */
    public NBTTagCompound getNBTCompound() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("name", serverName);
        nbttagcompound.setString("ip", serverIP);

        if (serverIcon != null) {
            nbttagcompound.setString("icon", serverIcon);
        }

        if (resourceMode == ServerData.ServerResourceMode.ENABLED) {
            nbttagcompound.setBoolean("acceptTextures", true);
        } else if (resourceMode == ServerData.ServerResourceMode.DISABLED) {
            nbttagcompound.setBoolean("acceptTextures", false);
        }

        return nbttagcompound;
    }

    public ServerData.ServerResourceMode getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(ServerData.ServerResourceMode mode) {
        resourceMode = mode;
    }

    /**
     * Returns the base-64 encoded representation of the server's icon, or null if not available
     */
    public String getBase64EncodedIconData() {
        return serverIcon;
    }

    public void setBase64EncodedIconData(String icon) {
        serverIcon = icon;
    }

    /**
     * Return true if the server is a LAN server
     */
    public boolean isOnLAN() {
        return lanServer;
    }

    public void copyFrom(ServerData serverDataIn) {
        serverIP = serverDataIn.serverIP;
        serverName = serverDataIn.serverName;
        setResourceMode(serverDataIn.getResourceMode());
        serverIcon = serverDataIn.serverIcon;
        lanServer = serverDataIn.lanServer;
    }

    public enum ServerResourceMode {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final IChatComponent motd;

        ServerResourceMode(String name) {
            motd = new ChatComponentTranslation("addServer.resourcePack." + name);
        }

        public IChatComponent getMotd() {
            return motd;
        }
    }
}
