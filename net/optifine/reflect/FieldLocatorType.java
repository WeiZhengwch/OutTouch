package net.optifine.reflect;

import net.optifine.Log;

import java.lang.reflect.Field;

public class FieldLocatorType implements IFieldLocator {
    private final int targetFieldIndex;
    private ReflectorClass reflectorClass;
    private Class targetFieldType;

    public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType) {
        this(reflectorClass, targetFieldType, 0);
    }

    public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType, int targetFieldIndex) {
        this.reflectorClass = null;
        this.targetFieldType = null;
        this.reflectorClass = reflectorClass;
        this.targetFieldType = targetFieldType;
        this.targetFieldIndex = targetFieldIndex;
    }

    public Field getField() {
        Class oclass = reflectorClass.getTargetClass();

        if (oclass == null) {
            return null;
        } else {
            try {
                Field[] afield = oclass.getDeclaredFields();
                int i = 0;

                for (Field field : afield) {
                    if (field.getType() == targetFieldType) {
                        if (i == targetFieldIndex) {
                            field.setAccessible(true);
                            return field;
                        }

                        ++i;
                    }
                }

                Log.log("(Reflector) Field not present: " + oclass.getName() + ".(type: " + targetFieldType + ", index: " + targetFieldIndex + ")");
                return null;
            } catch (Throwable securityexception) {
                securityexception.printStackTrace();
                return null;
            }
        }
    }
}
