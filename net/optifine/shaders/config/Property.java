package net.optifine.shaders.config;

import net.minecraft.src.Config;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Properties;

public class Property {
    private final int defaultValue;
    private final String propertyName;
    private final String[] propertyValues;
    private final String userName;
    private final String[] userValues;
    private int value;

    public Property(String propertyName, String[] propertyValues, String userName, String[] userValues, int defaultValue) {
        this.propertyName = propertyName;
        this.propertyValues = propertyValues;
        this.userName = userName;
        this.userValues = userValues;
        this.defaultValue = defaultValue;

        if (propertyValues.length != userValues.length) {
            throw new IllegalArgumentException("Property and user values have different lengths: " + propertyValues.length + " != " + userValues.length);
        } else if (defaultValue >= 0 && defaultValue < propertyValues.length) {
            value = defaultValue;
        } else {
            throw new IllegalArgumentException("Invalid default value: " + defaultValue);
        }
    }

    public boolean setPropertyValue(String propVal) {
        if (propVal == null) {
            value = defaultValue;
            return false;
        } else {
            value = ArrayUtils.indexOf(propertyValues, propVal);

            if (value >= 0 && value < propertyValues.length) {
                return true;
            } else {
                value = defaultValue;
                return false;
            }
        }
    }

    public void nextValue(boolean forward) {
        int i = 0;
        int j = propertyValues.length - 1;
        value = Config.limit(value, i, j);

        if (forward) {
            ++value;

            if (value > j) {
                value = i;
            }
        } else {
            --value;

            if (value < i) {
                value = j;
            }
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int val) {
        value = val;

        if (value < 0 || value >= propertyValues.length) {
            value = defaultValue;
        }
    }

    public String getUserValue() {
        return userValues[value];
    }

    public String getPropertyValue() {
        return propertyValues[value];
    }

    public String getUserName() {
        return userName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void resetValue() {
        value = defaultValue;
    }

    public boolean loadFrom(Properties props) {
        resetValue();

        if (props == null) {
            return false;
        } else {
            String s = props.getProperty(propertyName);
            return s != null && setPropertyValue(s);
        }
    }

    public void saveTo(Properties props) {
        if (props != null) {
            props.setProperty(getPropertyName(), getPropertyValue());
        }
    }

    public String toString() {
        return propertyName + "=" + getPropertyValue() + " [" + Config.arrayToString(propertyValues) + "], value: " + value;
    }
}
