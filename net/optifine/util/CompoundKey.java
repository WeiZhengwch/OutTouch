package net.optifine.util;

import net.minecraft.src.Config;

import java.util.Objects;

public class CompoundKey {
    private final Object[] keys;
    private int hashcode;

    public CompoundKey(Object[] keys) {
        hashcode = 0;
        this.keys = keys.clone();
    }

    public CompoundKey(Object k1, Object k2) {
        this(new Object[]{k1, k2});
    }

    public CompoundKey(Object k1, Object k2, Object k3) {
        this(new Object[]{k1, k2, k3});
    }

    private static boolean compareKeys(Object key1, Object key2) {
        return Objects.equals(key1, key2);
    }

    public int hashCode() {
        if (hashcode == 0) {
            hashcode = 7;

            for (Object object : keys) {
                if (object != null) {
                    hashcode = 31 * hashcode + object.hashCode();
                }
            }
        }

        return hashcode;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof CompoundKey compoundkey)) {
            return false;
        } else {
            Object[] aobject = compoundkey.getKeys();

            if (aobject.length != keys.length) {
                return false;
            } else {
                for (int i = 0; i < keys.length; ++i) {
                    if (!compareKeys(keys[i], aobject[i])) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private Object[] getKeys() {
        return keys;
    }

    public Object[] getKeysCopy() {
        return keys.clone();
    }

    public String toString() {
        return "[" + Config.arrayToString(keys) + "]";
    }
}
