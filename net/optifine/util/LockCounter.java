package net.optifine.util;

public class LockCounter {
    private int lockCount;

    public boolean lock() {
        ++lockCount;
        return lockCount == 1;
    }

    public boolean unlock() {
        if (lockCount <= 0) {
            return false;
        } else {
            --lockCount;
            return lockCount == 0;
        }
    }

    public boolean isLocked() {
        return lockCount > 0;
    }

    public int getLockCount() {
        return lockCount;
    }

    public String toString() {
        return "lockCount: " + lockCount;
    }
}
