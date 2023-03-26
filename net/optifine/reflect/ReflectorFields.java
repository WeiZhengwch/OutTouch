package net.optifine.reflect;

public class ReflectorFields {
    private final ReflectorClass reflectorClass;
    private final Class fieldType;
    private int fieldCount;
    private ReflectorField[] reflectorFields;

    public ReflectorFields(ReflectorClass reflectorClass, Class fieldType, int fieldCount) {
        this.reflectorClass = reflectorClass;
        this.fieldType = fieldType;

        if (reflectorClass.exists()) {
            if (fieldType != null) {
                reflectorFields = new ReflectorField[fieldCount];

                for (int i = 0; i < reflectorFields.length; ++i) {
                    reflectorFields[i] = new ReflectorField(reflectorClass, fieldType, i);
                }
            }
        }
    }

    public ReflectorClass getReflectorClass() {
        return reflectorClass;
    }

    public Class getFieldType() {
        return fieldType;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public ReflectorField getReflectorField(int index) {
        return index >= 0 && index < reflectorFields.length ? reflectorFields[index] : null;
    }
}
