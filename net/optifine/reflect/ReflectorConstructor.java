package net.optifine.reflect;

import net.optifine.Log;
import net.optifine.util.ArrayUtils;

import java.lang.reflect.Constructor;

public class ReflectorConstructor implements IResolvable {
    private final ReflectorClass reflectorClass;
    private final Class[] parameterTypes;
    private boolean checked;
    private Constructor targetConstructor;

    public ReflectorConstructor(ReflectorClass reflectorClass, Class[] parameterTypes) {
        this.reflectorClass = reflectorClass;
        this.parameterTypes = parameterTypes;
        ReflectorResolver.register(this);
    }

    private static Constructor findConstructor(Class cls, Class[] paramTypes) {
        Constructor[] aconstructor = cls.getDeclaredConstructors();

        for (Constructor constructor : aconstructor) {
            Class[] aclass = constructor.getParameterTypes();

            if (Reflector.matchesTypes(paramTypes, aclass)) {
                return constructor;
            }
        }

        return null;
    }

    public Constructor getTargetConstructor() {
        if (checked) {
            return targetConstructor;
        } else {
            checked = true;
            Class oclass = reflectorClass.getTargetClass();

            if (oclass == null) {
                return null;
            } else {
                try {
                    targetConstructor = findConstructor(oclass, parameterTypes);

                    if (targetConstructor == null) {
                        Log.dbg("(Reflector) Constructor not present: " + oclass.getName() + ", params: " + ArrayUtils.arrayToString(parameterTypes));
                    }

                    if (targetConstructor != null) {
                        targetConstructor.setAccessible(true);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                return targetConstructor;
            }
        }
    }

    public boolean exists() {
        return checked ? targetConstructor != null : getTargetConstructor() != null;
    }

    public void deactivate() {
        checked = true;
        targetConstructor = null;
    }

    public Object newInstance(Object... params) {
        return Reflector.newInstance(this, params);
    }

    public void resolve() {
        Constructor constructor = getTargetConstructor();
    }
}
