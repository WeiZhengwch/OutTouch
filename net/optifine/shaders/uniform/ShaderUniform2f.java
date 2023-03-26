package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform2f extends ShaderUniformBase {
    private static final float VALUE_UNKNOWN = -3.4028235E38F;
    private float[][] programValues;

    public ShaderUniform2f(String name) {
        super(name);
        resetValue();
    }

    public void setValue(float v0, float v1) {
        int i = getProgram();
        float[] afloat = programValues[i];

        if (afloat[0] != v0 || afloat[1] != v1) {
            afloat[0] = v0;
            afloat[1] = v1;
            int j = getLocation();

            if (j >= 0) {
                ARBShaderObjects.glUniform2fARB(j, v0, v1);
                checkGLError();
            }
        }
    }

    public float[] getValue() {
        int i = getProgram();
        float[] afloat = programValues[i];
        return afloat;
    }

    protected void onProgramSet(int program) {
        if (program >= programValues.length) {
            float[][] afloat = programValues;
            float[][] afloat1 = new float[program + 10][];
            System.arraycopy(afloat, 0, afloat1, 0, afloat.length);
            programValues = afloat1;
        }

        if (programValues[program] == null) {
            programValues[program] = new float[]{-3.4028235E38F, -3.4028235E38F};
        }
    }

    protected void resetValue() {
        programValues = new float[][]{{-3.4028235E38F, -3.4028235E38F}};
    }
}
