package net.optifine.shaders;

public class MultiTexID {
    public int base;
    public int norm;
    public int spec;

    public MultiTexID(int baseTex, int normTex, int specTex) {
        base = baseTex;
        norm = normTex;
        spec = specTex;
    }
}
