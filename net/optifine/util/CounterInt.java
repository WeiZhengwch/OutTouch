package net.optifine.util;

public class CounterInt {
    private final int startValue;
    private int value;

    public CounterInt(int startValue) {
        this.startValue = startValue;
        value = startValue;
    }

    public synchronized int nextValue() {
        int i = value++;
        return i;
    }

    public synchronized void reset() {
        value = startValue;
    }

    public int getValue() {
        return value;
    }
}
