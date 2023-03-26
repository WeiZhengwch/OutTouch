package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

import java.nio.FloatBuffer;

public class ShaderUniformM4 extends ShaderUniformBase {
    private boolean transpose;
    private FloatBuffer matrix;

    public ShaderUniformM4(String name) {
        super(name);
    }

    public void setValue(boolean transpose, FloatBuffer matrix) {
        this.transpose = transpose;
        this.matrix = matrix;
        int i = getLocation();

        if (i >= 0) {
            ARBShaderObjects.glUniformMatrix4ARB(i, transpose, matrix);
            checkGLError();
        }
    }

    public float getValue(int row, int col) {
        if (matrix == null) {
            return 0.0F;
        } else {
            int i = transpose ? col * 4 + row : row * 4 + col;
            float f = matrix.get(i);
            return f;
        }
    }

    protected void onProgramSet(int program) {
    }

    protected void resetValue() {
        matrix = null;
    }
}
