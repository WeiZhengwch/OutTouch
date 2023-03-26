package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StatisticsFile extends StatFileWriter {
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final File statsFile;
    private final Set<StatBase> field_150888_e = Sets.newHashSet();
    private int field_150885_f = -300;
    private boolean field_150886_g;

    public StatisticsFile(MinecraftServer serverIn, File statsFileIn) {
        mcServer = serverIn;
        statsFile = statsFileIn;
    }

    public static String dumpJson(Map<StatBase, TupleIntJsonSerializable> p_150880_0_) {
        JsonObject jsonobject = new JsonObject();

        for (Entry<StatBase, TupleIntJsonSerializable> entry : p_150880_0_.entrySet()) {
            if (entry.getValue().getJsonSerializableValue() != null) {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject1.addProperty("value", entry.getValue().getIntegerValue());

                try {
                    jsonobject1.add("progress", entry.getValue().getJsonSerializableValue().getSerializableElement());
                } catch (Throwable throwable) {
                    logger.warn("Couldn't save statistic " + entry.getKey().getStatName() + ": error serializing progress", throwable);
                }

                jsonobject.add(entry.getKey().statId, jsonobject1);
            } else {
                jsonobject.addProperty(entry.getKey().statId, entry.getValue().getIntegerValue());
            }
        }

        return jsonobject.toString();
    }

    public void readStatFile() {
        if (statsFile.isFile()) {
            try {
                statsData.clear();
                statsData.putAll(parseJson(FileUtils.readFileToString(statsFile)));
            } catch (IOException ioexception) {
                logger.error("Couldn't read statistics file " + statsFile, ioexception);
            } catch (JsonParseException jsonparseexception) {
                logger.error("Couldn't parse statistics file " + statsFile, jsonparseexception);
            }
        }
    }

    public void saveStatFile() {
        try {
            FileUtils.writeStringToFile(statsFile, dumpJson(statsData));
        } catch (IOException ioexception) {
            logger.error("Couldn't save stats", ioexception);
        }
    }

    /**
     * Triggers the logging of an achievement and attempts to announce to server
     */
    public void unlockAchievement(EntityPlayer playerIn, StatBase statIn, int p_150873_3_) {
        int i = statIn.isAchievement() ? readStat(statIn) : 0;
        super.unlockAchievement(playerIn, statIn, p_150873_3_);
        field_150888_e.add(statIn);

        if (statIn.isAchievement() && i == 0 && p_150873_3_ > 0) {
            field_150886_g = true;

            if (mcServer.isAnnouncingPlayerAchievements()) {
                mcServer.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.achievement", playerIn.getDisplayName(), statIn.createChatComponent()));
            }
        }

        if (statIn.isAchievement() && i > 0 && p_150873_3_ == 0) {
            field_150886_g = true;

            if (mcServer.isAnnouncingPlayerAchievements()) {
                mcServer.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.achievement.taken", playerIn.getDisplayName(), statIn.createChatComponent()));
            }
        }
    }

    public Set<StatBase> func_150878_c() {
        Set<StatBase> set = Sets.newHashSet(field_150888_e);
        field_150888_e.clear();
        field_150886_g = false;
        return set;
    }

    public Map<StatBase, TupleIntJsonSerializable> parseJson(String p_150881_1_) {
        JsonElement jsonelement = (new JsonParser()).parse(p_150881_1_);

        if (!jsonelement.isJsonObject()) {
            return Maps.newHashMap();
        } else {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            Map<StatBase, TupleIntJsonSerializable> map = Maps.newHashMap();

            for (Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                StatBase statbase = StatList.getOneShotStat(entry.getKey());

                if (statbase != null) {
                    TupleIntJsonSerializable tupleintjsonserializable = new TupleIntJsonSerializable();

                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                        tupleintjsonserializable.setIntegerValue(entry.getValue().getAsInt());
                    } else if (entry.getValue().isJsonObject()) {
                        JsonObject jsonobject1 = entry.getValue().getAsJsonObject();

                        if (jsonobject1.has("value") && jsonobject1.get("value").isJsonPrimitive() && jsonobject1.get("value").getAsJsonPrimitive().isNumber()) {
                            tupleintjsonserializable.setIntegerValue(jsonobject1.getAsJsonPrimitive("value").getAsInt());
                        }

                        if (jsonobject1.has("progress") && statbase.func_150954_l() != null) {
                            try {
                                Constructor<? extends IJsonSerializable> constructor = statbase.func_150954_l().getConstructor();
                                IJsonSerializable ijsonserializable = constructor.newInstance();
                                ijsonserializable.fromJson(jsonobject1.get("progress"));
                                tupleintjsonserializable.setJsonSerializableValue(ijsonserializable);
                            } catch (Throwable throwable) {
                                logger.warn("Invalid statistic progress in " + statsFile, throwable);
                            }
                        }
                    }

                    map.put(statbase, tupleintjsonserializable);
                } else {
                    logger.warn("Invalid statistic in " + statsFile + ": Don't know what " + entry.getKey() + " is");
                }
            }

            return map;
        }
    }

    public void func_150877_d() {
        for (StatBase statbase : statsData.keySet()) {
            field_150888_e.add(statbase);
        }
    }

    public void func_150876_a(EntityPlayerMP p_150876_1_) {
        int i = mcServer.getTickCounter();
        Map<StatBase, Integer> map = Maps.newHashMap();

        if (field_150886_g || i - field_150885_f > 300) {
            field_150885_f = i;

            for (StatBase statbase : func_150878_c()) {
                map.put(statbase, readStat(statbase));
            }
        }

        p_150876_1_.playerNetServerHandler.sendPacket(new S37PacketStatistics(map));
    }

    public void sendAchievements(EntityPlayerMP player) {
        Map<StatBase, Integer> map = Maps.newHashMap();

        for (Achievement achievement : AchievementList.achievementList) {
            if (hasAchievementUnlocked(achievement)) {
                map.put(achievement, readStat(achievement));
                field_150888_e.remove(achievement);
            }
        }

        player.playerNetServerHandler.sendPacket(new S37PacketStatistics(map));
    }

    public boolean func_150879_e() {
        return field_150886_g;
    }
}
