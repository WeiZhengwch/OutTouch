package net.optifine.util;

import java.util.ArrayList;

public class CompactArrayList {
    private ArrayList list;
    private int initialCapacity;
    private float loadFactor;
    private int countValid;

    public CompactArrayList() {
        this(10, 0.75F);
    }

    public CompactArrayList(int initialCapacity) {
        this(initialCapacity, 0.75F);
    }

    public CompactArrayList(int initialCapacity, float loadFactor) {
        list = null;
        this.initialCapacity = 0;
        this.loadFactor = 1.0F;
        countValid = 0;
        list = new ArrayList(initialCapacity);
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
    }

    public void add(int index, Object element) {
        if (element != null) {
            ++countValid;
        }

        list.add(index, element);
    }

    public boolean add(Object element) {
        if (element != null) {
            ++countValid;
        }

        return list.add(element);
    }

    public Object set(int index, Object element) {
        Object object = list.set(index, element);

        if (element != object) {
            if (object == null) {
                ++countValid;
            }

            if (element == null) {
                --countValid;
            }
        }

        return object;
    }

    public Object remove(int index) {
        Object object = list.remove(index);

        if (object != null) {
            --countValid;
        }

        return object;
    }

    public void clear() {
        list.clear();
        countValid = 0;
    }

    public void compact() {
        if (countValid <= 0 && list.size() <= 0) {
            clear();
        } else if (list.size() > initialCapacity) {
            float f = (float) countValid / (float) list.size();

            if (f <= loadFactor) {
                int i = 0;

                for (int j = 0; j < list.size(); ++j) {
                    Object object = list.get(j);

                    if (object != null) {
                        if (j != i) {
                            list.set(i, object);
                        }

                        ++i;
                    }
                }

                for (int k = list.size() - 1; k >= i; --k) {
                    list.remove(k);
                }
            }
        }
    }

    public boolean contains(Object elem) {
        return list.contains(elem);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public int getCountValid() {
        return countValid;
    }
}
