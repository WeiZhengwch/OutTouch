package net.optifine.reflect;

import net.optifine.Log;

public class ReflectorClass implements IResolvable {
    private final String targetClassName;
    private boolean checked;
    private Class targetClass;

    public ReflectorClass(String targetClassName) {
        this.targetClassName = targetClassName;
        ReflectorResolver.register(this);
    }

    public ReflectorClass(Class targetClass) {
        this.targetClass = targetClass;
        targetClassName = targetClass.getName();
        checked = true;
    }

    public Class getTargetClass() {
        if (!checked) {
            checked = true;

            try {
                targetClass = Class.forName(targetClassName);
            } catch (ClassNotFoundException var2) {
                Log.log("(Reflector) Class not present: " + targetClassName);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        return targetClass;
    }

    public boolean exists() {
        return getTargetClass() != null;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public boolean isInstance(Object obj) {
        return getTargetClass() != null && getTargetClass().isInstance(obj);
    }

    public ReflectorField makeField(String name) {
        return new ReflectorField(this, name);
    }

    public ReflectorMethod makeMethod(String name) {
        return new ReflectorMethod(this, name);
    }

    public ReflectorMethod makeMethod(String name, Class[] paramTypes) {
        return new ReflectorMethod(this, name, paramTypes);
    }

    public void resolve() {
        Class oclass = getTargetClass();
    }
}
