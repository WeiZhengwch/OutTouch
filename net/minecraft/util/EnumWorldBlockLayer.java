package net.minecraft.util;

public enum EnumWorldBlockLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String layerName;

    EnumWorldBlockLayer(String layerNameIn) {
        layerName = layerNameIn;
    }

    public String toString() {
        return layerName;
    }
}
