package net.optifine.reflect;

import net.optifine.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectorMethod implements IResolvable {
    private ReflectorClass reflectorClass;
    private String targetMethodName;
    private Class[] targetMethodParameterTypes;
    private boolean checked;
    private Method targetMethod;

    public ReflectorMethod(ReflectorClass reflectorClass, String targetMethodName) {
        this(reflectorClass, targetMethodName, null);
    }

    public ReflectorMethod(ReflectorClass reflectorClass, String targetMethodName, Class[] targetMethodParameterTypes) {
        this.reflectorClass = null;
        this.targetMethodName = null;
        this.targetMethodParameterTypes = null;
        checked = false;
        targetMethod = null;
        this.reflectorClass = reflectorClass;
        this.targetMethodName = targetMethodName;
        this.targetMethodParameterTypes = targetMethodParameterTypes;
        ReflectorResolver.register(this);
    }

    public static Method getMethod(Class cls, String methodName, Class[] paramTypes) {
        Method[] amethod = cls.getDeclaredMethods();

        for (Method method : amethod) {
            if (method.getName().equals(methodName)) {
                Class[] aclass = method.getParameterTypes();

                if (Reflector.matchesTypes(paramTypes, aclass)) {
                    return method;
                }
            }
        }

        return null;
    }

    public static Method[] getMethods(Class cls, String methodName) {
        List list = new ArrayList();
        Method[] amethod = cls.getDeclaredMethods();

        for (Method method : amethod) {
            if (method.getName().equals(methodName)) {
                list.add(method);
            }
        }

        return (Method[]) list.toArray(new Method[list.size()]);
    }

    public Method getTargetMethod() {
        if (checked) {
            return targetMethod;
        } else {
            checked = true;
            Class oclass = reflectorClass.getTargetClass();

            if (oclass == null) {
                return null;
            } else {
                try {
                    if (targetMethodParameterTypes == null) {
                        Method[] amethod = getMethods(oclass, targetMethodName);

                        if (amethod.length <= 0) {
                            Log.log("(Reflector) Method not present: " + oclass.getName() + "." + targetMethodName);
                            return null;
                        }

                        if (amethod.length > 1) {
                            Log.warn("(Reflector) More than one method found: " + oclass.getName() + "." + targetMethodName);

                            for (Method method : amethod) {
                                Log.warn("(Reflector)  - " + method);
                            }

                            return null;
                        }

                        targetMethod = amethod[0];
                    } else {
                        targetMethod = getMethod(oclass, targetMethodName, targetMethodParameterTypes);
                    }

                    if (targetMethod == null) {
                        Log.log("(Reflector) Method not present: " + oclass.getName() + "." + targetMethodName);
                        return null;
                    } else {
                        targetMethod.setAccessible(true);
                        return targetMethod;
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return null;
                }
            }
        }
    }

    public boolean exists() {
        return checked ? targetMethod != null : getTargetMethod() != null;
    }

    public Class getReturnType() {
        Method method = getTargetMethod();
        return method == null ? null : method.getReturnType();
    }

    public void deactivate() {
        checked = true;
        targetMethod = null;
    }

    public Object call(Object... params) {
        return Reflector.call(this, params);
    }

    public boolean callBoolean(Object... params) {
        return Reflector.callBoolean(this, params);
    }

    public int callInt(Object... params) {
        return Reflector.callInt(this, params);
    }

    public float callFloat(Object... params) {
        return Reflector.callFloat(this, params);
    }

    public double callDouble(Object... params) {
        return Reflector.callDouble(this, params);
    }

    public String callString(Object... params) {
        return Reflector.callString(this, params);
    }

    public Object call(Object param) {
        return Reflector.call(this, param);
    }

    public boolean callBoolean(Object param) {
        return Reflector.callBoolean(this, param);
    }

    public int callInt(Object param) {
        return Reflector.callInt(this, param);
    }

    public float callFloat(Object param) {
        return Reflector.callFloat(this, param);
    }

    public double callDouble(Object param) {
        return Reflector.callDouble(this, param);
    }

    public String callString1(Object param) {
        return Reflector.callString(this, param);
    }

    public void callVoid(Object... params) {
        Reflector.callVoid(this, params);
    }

    public void resolve() {
        Method method = getTargetMethod();
    }
}
