package me.banendy.client;

import me.banendy.client.misc.Cache;
import me.banendy.client.mod.ModManager;
import org.lwjgl.opengl.Display;

import java.io.IOException;

public class MainClient {
    public static String VERSION = "6.99B";
    public static String NAME = "OutTouch";
    public static ModManager ModManager;
    public static boolean isloaded;

    public static void loader() {
        Thread AsyncLoader = new Thread(() -> {
            ModManager = new ModManager();
            ModManager.load();
            isloaded = true;
        });
        AsyncLoader.start();
    }

    public static void stop() {
        Display.setTitle("Shutting down");
    }

}
