package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform1f extends ShaderUniformBase {
    private static final float VALUE_UNKNOWN = -3.4028235E38F;
    private float[] programValues;

    public ShaderUniform1f(String name) {
        super(name);
        resetValue();
    }

    public float getValue() {
        int i = getProgram();
        float f = programValues[i];
        return f;
    }

    public void setValue(float valueNew) {
        int i = getProgram();
        float f = programValues[i];

        if (valueNew != f) {
            programValues[i] = valueNew;
            int j = getLocation();

            if (j >= 0) {
                ARBShaderObjects.glUniform1fARB(j, valueNew);
                checkGLError();
            }
        }
    }

    protected void onProgramSet(int program) {
        if (program >= programValues.length) {
            float[] afloat = programValues;
            float[] afloat1 = new float[program + 10];
            System.arraycopy(afloat, 0, afloat1, 0, afloat.length);

            for (int i = afloat.length; i < afloat1.length; ++i) {
                afloat1[i] = -3.4028235E38F;
            }

            programValues = afloat1;
        }
    }

    protected void resetValue() {
        programValues = new float[]{-3.4028235E38F};
    }
}
