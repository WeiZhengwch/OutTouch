package me.banendy.client.misc;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MouseHelper;

public class RawInputHandler {
    public static Controller[] controllers;
    public static Mouse mouse;
    public static int dx = 0;
    public static int dy = 0;

    public static void init() {
        controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        startThread();
    }

    public static void getMouse() {
        for (int i = 0; i < controllers.length && mouse == null; i++) {
            if (controllers[i].getType() == Controller.Type.MOUSE) {
                controllers[i].poll();
                if (((Mouse) controllers[i]).getX().getPollData() != 0.0 || ((Mouse) controllers[i]).getY().getPollData() != 0.0) {
                    mouse = (Mouse) controllers[i];
                }
            }
        }
    }

    public static void startThread() {
        Thread inputThread = new Thread(() -> {
            while(true){
                if (mouse != null && Minecraft.getMinecraft().currentScreen == null) {
                    mouse.poll();
                    dx += (int)mouse.getX().getPollData();
                    dy += (int)mouse.getY().getPollData();
                } else if (mouse != null) {
                    mouse.poll();
                } else {
                    getMouse();
                }

                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        inputThread.setName("inputThread");
        inputThread.start();
    }
}
