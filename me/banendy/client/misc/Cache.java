package me.banendy.client.misc;

import net.minecraft.client.Minecraft;
import com.github.benmanes.caffeine.cache.*;

import java.util.concurrent.TimeUnit;

public class Cache {
    static Minecraft mc = Minecraft.getMinecraft();
//    public static boolean isneedRefresh;
    public static com.github.benmanes.caffeine.cache.Cache<String, Double> Cache = Caffeine.newBuilder()
            .initialCapacity(10)
            .expireAfterWrite(200, TimeUnit.MILLISECONDS)
            .build();

    public void init() {
    }

    public static void RefreshCache() {
        Cache.put("PlayerPosX", mc.thePlayer.posX);
        Cache.put("PlayerPosY", mc.thePlayer.posY);
        Cache.put("PlayerPosZ", mc.thePlayer.posZ);
        Cache.put("PlayerHealth", (double) mc.thePlayer.getHealth());
    }
}
