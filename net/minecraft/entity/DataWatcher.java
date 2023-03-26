package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Rotations;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataWatcher {
    private static final Map<Class<?>, Integer> dataTypes = Maps.newHashMap();

    static {
        dataTypes.put(Byte.class, 0);
        dataTypes.put(Short.class, 1);
        dataTypes.put(Integer.class, 2);
        dataTypes.put(Float.class, 3);
        dataTypes.put(String.class, 4);
        dataTypes.put(ItemStack.class, 5);
        dataTypes.put(BlockPos.class, 6);
        dataTypes.put(Rotations.class, 7);
    }

    private final Entity owner;
    private final Map<Integer, DataWatcher.WatchableObject> watchedObjects = Maps.newHashMap();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    public BiomeGenBase spawnBiome = BiomeGenBase.plains;
    public BlockPos spawnPosition = BlockPos.ORIGIN;
    /**
     * When isBlank is true the DataWatcher is not watching any objects
     */
    private boolean isBlank = true;
    /**
     * true if one or more object was changed
     */
    private boolean objectChanged;

    public DataWatcher(Entity owner) {
        this.owner = owner;
    }

    /**
     * Writes the list of watched objects (entity attribute of type {byte, short, int, float, string, ItemStack,
     * ChunkCoordinates}) to the specified PacketBuffer
     */
    public static void writeWatchedListToPacketBuffer(List<DataWatcher.WatchableObject> objectsList, PacketBuffer buffer) throws IOException {
        if (objectsList != null) {
            for (DataWatcher.WatchableObject datawatcher$watchableobject : objectsList) {
                writeWatchableObjectToPacketBuffer(buffer, datawatcher$watchableobject);
            }
        }

        buffer.writeByte(127);
    }

    /**
     * Writes a watchable object (entity attribute of type {byte, short, int, float, string, ItemStack,
     * ChunkCoordinates}) to the specified PacketBuffer
     */
    private static void writeWatchableObjectToPacketBuffer(PacketBuffer buffer, DataWatcher.WatchableObject object) {
        int i = (object.getObjectType() << 5 | object.getDataValueId() & 31) & 255;
        buffer.writeByte(i);

        switch (object.getObjectType()) {
            case 0 -> buffer.writeByte(((Byte) object.getObject()).intValue());
            case 1 -> buffer.writeShort(((Short) object.getObject()).intValue());
            case 2 -> buffer.writeInt((Integer) object.getObject());
            case 3 -> buffer.writeFloat((Float) object.getObject());
            case 4 -> buffer.writeString((String) object.getObject());
            case 5 -> {
                ItemStack itemstack = (ItemStack) object.getObject();
                buffer.writeItemStackToBuffer(itemstack);
            }
            case 6 -> {
                BlockPos blockpos = (BlockPos) object.getObject();
                buffer.writeInt(blockpos.getX());
                buffer.writeInt(blockpos.getY());
                buffer.writeInt(blockpos.getZ());
            }
            case 7 -> {
                Rotations rotations = (Rotations) object.getObject();
                buffer.writeFloat(rotations.getX());
                buffer.writeFloat(rotations.getY());
                buffer.writeFloat(rotations.getZ());
            }
        }
    }

    public static List<DataWatcher.WatchableObject> readWatchedListFromPacketBuffer(PacketBuffer buffer) throws IOException {
        List<DataWatcher.WatchableObject> list = null;

        for (int i = buffer.readByte(); i != 127; i = buffer.readByte()) {
            if (list == null) {
                list = Lists.newArrayList();
            }

            int j = (i & 224) >> 5;
            int k = i & 31;
            DataWatcher.WatchableObject datawatcher$watchableobject = null;

            switch (j) {
                case 0 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readByte());
                case 1 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readShort());
                case 2 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readInt());
                case 3 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readFloat());
                case 4 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readStringFromBuffer(32767));
                case 5 -> datawatcher$watchableobject = new WatchableObject(j, k, buffer.readItemStackFromBuffer());
                case 6 -> {
                    int l = buffer.readInt();
                    int i1 = buffer.readInt();
                    int j1 = buffer.readInt();
                    datawatcher$watchableobject = new WatchableObject(j, k, new BlockPos(l, i1, j1));
                }
                case 7 -> {
                    float f = buffer.readFloat();
                    float f1 = buffer.readFloat();
                    float f2 = buffer.readFloat();
                    datawatcher$watchableobject = new WatchableObject(j, k, new Rotations(f, f1, f2));
                }
            }

            list.add(datawatcher$watchableobject);
        }

        return list;
    }

    public <T> void addObject(int id, T object) {
        Integer integer = dataTypes.get(object.getClass());

        if (integer == null) {
            throw new IllegalArgumentException("Unknown data type: " + object.getClass());
        } else if (id > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + id + "! (Max is " + 31 + ")");
        } else if (watchedObjects.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id value for " + id + "!");
        } else {
            DataWatcher.WatchableObject datawatcher$watchableobject = new DataWatcher.WatchableObject(integer, id, object);
            lock.writeLock().lock();
            watchedObjects.put(id, datawatcher$watchableobject);
            lock.writeLock().unlock();
            isBlank = false;
        }
    }

    /**
     * Add a new object for the DataWatcher to watch, using the specified data type.
     */
    public void addObjectByDataType(int id, int type) {
        DataWatcher.WatchableObject datawatcher$watchableobject = new DataWatcher.WatchableObject(type, id, null);
        lock.writeLock().lock();
        watchedObjects.put(id, datawatcher$watchableobject);
        lock.writeLock().unlock();
        isBlank = false;
    }

    /**
     * gets the bytevalue of a watchable object
     */
    public byte getWatchableObjectByte(int id) {
        return (Byte) getWatchedObject(id).getObject();
    }

    public short getWatchableObjectShort(int id) {
        return (Short) getWatchedObject(id).getObject();
    }

    /**
     * gets a watchable object and returns it as a Integer
     */
    public int getWatchableObjectInt(int id) {
        return (Integer) getWatchedObject(id).getObject();
    }

    public float getWatchableObjectFloat(int id) {
        return (Float) getWatchedObject(id).getObject();
    }

    /**
     * gets a watchable object and returns it as a String
     */
    public String getWatchableObjectString(int id) {
        return (String) getWatchedObject(id).getObject();
    }

    /**
     * Get a watchable object as an ItemStack.
     */
    public ItemStack getWatchableObjectItemStack(int id) {
        return (ItemStack) getWatchedObject(id).getObject();
    }

    /**
     * is threadsafe, unless it throws an exception, then
     */
    private DataWatcher.WatchableObject getWatchedObject(int id) {
        lock.readLock().lock();
        DataWatcher.WatchableObject datawatcher$watchableobject;

        try {
            datawatcher$watchableobject = watchedObjects.get(id);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Synched entity data");
            crashreportcategory.addCrashSection("Data ID", id);
            throw new ReportedException(crashreport);
        }

        lock.readLock().unlock();
        return datawatcher$watchableobject;
    }

    public Rotations getWatchableObjectRotations(int id) {
        return (Rotations) getWatchedObject(id).getObject();
    }

    public <T> void updateObject(int id, T newData) {
        DataWatcher.WatchableObject datawatcher$watchableobject = getWatchedObject(id);

        if (ObjectUtils.notEqual(newData, datawatcher$watchableobject.getObject())) {
            datawatcher$watchableobject.setObject(newData);
            owner.onDataWatcherUpdate(id);
            datawatcher$watchableobject.setWatched(true);
            objectChanged = true;
        }
    }

    public void setObjectWatched(int id) {
        getWatchedObject(id).watched = true;
        objectChanged = true;
    }

    /**
     * true if one or more object was changed
     */
    public boolean hasObjectChanged() {
        return objectChanged;
    }

    public List<DataWatcher.WatchableObject> getChanged() {
        List<DataWatcher.WatchableObject> list = null;

        if (objectChanged) {
            lock.readLock().lock();

            for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
                if (datawatcher$watchableobject.isWatched()) {
                    datawatcher$watchableobject.setWatched(false);

                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(datawatcher$watchableobject);
                }
            }

            lock.readLock().unlock();
        }

        objectChanged = false;
        return list;
    }

    public void writeTo(PacketBuffer buffer) throws IOException {
        lock.readLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
            writeWatchableObjectToPacketBuffer(buffer, datawatcher$watchableobject);
        }

        lock.readLock().unlock();
        buffer.writeByte(127);
    }

    public List<DataWatcher.WatchableObject> getAllWatched() {
        List<DataWatcher.WatchableObject> list = null;
        lock.readLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : watchedObjects.values()) {
            if (list == null) {
                list = Lists.newArrayList();
            }

            list.add(datawatcher$watchableobject);
        }

        lock.readLock().unlock();
        return list;
    }

    public void updateWatchedObjectsFromList(List<DataWatcher.WatchableObject> p_75687_1_) {
        lock.writeLock().lock();

        for (DataWatcher.WatchableObject datawatcher$watchableobject : p_75687_1_) {
            DataWatcher.WatchableObject datawatcher$watchableobject1 = watchedObjects.get(datawatcher$watchableobject.getDataValueId());

            if (datawatcher$watchableobject1 != null) {
                datawatcher$watchableobject1.setObject(datawatcher$watchableobject.getObject());
                owner.onDataWatcherUpdate(datawatcher$watchableobject.getDataValueId());
            }
        }

        lock.writeLock().unlock();
        objectChanged = true;
    }

    public boolean getIsBlank() {
        return isBlank;
    }

    public void func_111144_e() {
        objectChanged = false;
    }

    public static class WatchableObject {
        private final int objectType;
        private final int dataValueId;
        private Object watchedObject;
        private boolean watched;

        public WatchableObject(int type, int id, Object object) {
            dataValueId = id;
            watchedObject = object;
            objectType = type;
            watched = true;
        }

        public int getDataValueId() {
            return dataValueId;
        }

        public Object getObject() {
            return watchedObject;
        }

        public void setObject(Object object) {
            watchedObject = object;
        }

        public int getObjectType() {
            return objectType;
        }

        public boolean isWatched() {
            return watched;
        }

        public void setWatched(boolean watched) {
            this.watched = watched;
        }
    }
}
