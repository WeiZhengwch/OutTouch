package net.optifine;

import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;

import java.util.Properties;

public class CustomPanoramaProperties {
    private final String path;
    private final ResourceLocation[] panoramaLocations;
    private int weight = 1;
    private int overlay1Top = -2130706433;
    private int overlay1Bottom = 16777215;
    private final int overlay2Top;
    private int overlay2Bottom = Integer.MIN_VALUE;

    public CustomPanoramaProperties(String path, Properties props) {
        ConnectedParser connectedparser = new ConnectedParser("CustomPanorama");
        this.path = path;
        panoramaLocations = new ResourceLocation[6];

        for (int i = 0; i < panoramaLocations.length; ++i) {
            panoramaLocations[i] = new ResourceLocation(path + "/panorama_" + i + ".png");
        }

        weight = connectedparser.parseInt(props.getProperty("weight"), 1);
        overlay1Top = ConnectedParser.parseColor4(props.getProperty("overlay1.top"), -2130706433);
        overlay1Bottom = ConnectedParser.parseColor4(props.getProperty("overlay1.bottom"), 16777215);
        overlay2Top = ConnectedParser.parseColor4(props.getProperty("overlay2.top"), 0);
        overlay2Bottom = ConnectedParser.parseColor4(props.getProperty("overlay2.bottom"), Integer.MIN_VALUE);
    }

    public ResourceLocation[] getPanoramaLocations() {
        return panoramaLocations;
    }

    public int getWeight() {
        return weight;
    }

    public int getOverlay1Top() {
        return overlay1Top;
    }

    public int getOverlay1Bottom() {
        return overlay1Bottom;
    }

    public int getOverlay2Top() {
        return overlay2Top;
    }

    public int getOverlay2Bottom() {
        return overlay2Bottom;
    }

    public String toString() {
        return path + ", weight: " + weight + ", overlay: " + overlay1Top + " " + overlay1Bottom + " " + overlay2Top + " " + overlay2Bottom;
    }
}
