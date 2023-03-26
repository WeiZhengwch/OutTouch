package net.optifine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class IteratorCache {
    private static final Deque<IteratorCache.IteratorReusable<Object>> dequeIterators = new ArrayDeque();

    static {
        for (int i = 0; i < 1000; ++i) {
            IteratorCache.IteratorReadOnly iteratorcache$iteratorreadonly = new IteratorCache.IteratorReadOnly();
            dequeIterators.add(iteratorcache$iteratorreadonly);
        }
    }

    public static Iterator<Object> getReadOnly(List list) {
        synchronized (dequeIterators) {
            IteratorCache.IteratorReusable<Object> iteratorreusable = dequeIterators.pollFirst();

            if (iteratorreusable == null) {
                iteratorreusable = new IteratorCache.IteratorReadOnly();
            }

            iteratorreusable.setList(list);
            return iteratorreusable;
        }
    }

    private static void finished(IteratorCache.IteratorReusable<Object> iterator) {
        synchronized (dequeIterators) {
            if (dequeIterators.size() <= 1000) {
                iterator.setList(null);
                dequeIterators.addLast(iterator);
            }
        }
    }

    public interface IteratorReusable<E> extends Iterator<E> {
        void setList(List<E> var1);
    }

    public static class IteratorReadOnly implements IteratorCache.IteratorReusable<Object> {
        private List<Object> list;
        private int index;
        private boolean hasNext;

        public void setList(List<Object> list) {
            if (hasNext) {
                throw new RuntimeException("Iterator still used, oldList: " + this.list + ", newList: " + list);
            } else {
                this.list = list;
                index = 0;
                hasNext = list != null && index < list.size();
            }
        }

        public Object next() {
            if (!hasNext) {
                return null;
            } else {
                Object object = list.get(index);
                ++index;
                hasNext = index < list.size();
                return object;
            }
        }

        public boolean hasNext() {
            if (!hasNext) {
                finished(this);
                return false;
            } else {
                return hasNext;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
