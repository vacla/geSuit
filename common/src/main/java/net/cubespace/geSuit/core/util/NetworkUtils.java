package net.cubespace.geSuit.core.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.ByteStorable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

public class NetworkUtils {
    public static void writeUUID(DataOutput out, UUID uuid) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }
    
    public static UUID readUUID(DataInput in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
    
    public static <T extends Enum<T>> T readEnum(DataInput in, Class<T> clazz) throws IOException {
        int id = in.readUnsignedByte();
        EnumSet<T> eset = EnumSet.allOf(clazz);

        for (T value : eset) {
            if (value.ordinal() == id)
                return value;
        }

        return null;
    }
    
    public static <T extends Enum<T>> void writeEnum(DataOutput out, T enumValue) throws IOException {
        out.writeByte(enumValue.ordinal());
    }
    
    public static void writeObject(final DataOutput out, Object object) throws IOException {
        ObjectOutputStream out2 = new ObjectOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        });

        out2.writeObject(object);
        out2.close();
    }

    public static Object readObject(final DataInput in) throws IOException, ClassNotFoundException {
        ObjectInputStream in2 = new ObjectInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return in.readUnsignedByte();
            }
        });

        Object object = in2.readObject();
        in2.close();

        return object;
    }
    
    public static void writeInetAddress(DataOutput out, InetAddress address) throws IOException {
        byte[] data = address.getAddress();
        out.writeByte(data.length);
        out.write(data);
    }
    
    public static InetAddress readInetAddress(DataInput in) throws IOException {
        byte[] data = new byte[in.readUnsignedByte()];
        in.readFully(data);
        return InetAddress.getByAddress(data);
    }
    
    public static void writeTyped(DataOutput out, Object object) throws IOException {
        if (object == null) {
            out.writeByte(0);
        } else if (object instanceof Byte) {
            out.writeByte(1);
            out.writeByte((Byte)object);
        } else if (object instanceof Short) {
            out.writeByte(2);
            out.writeShort((Short)object);
        } else if (object instanceof Integer) {
            out.writeByte(3);
            out.writeInt((Integer)object);
        } else if (object instanceof Long) {
            out.writeByte(4);
            out.writeLong((Long)object);
        } else if (object instanceof Float) {
            out.writeByte(5);
            out.writeFloat((Float)object);
        } else if (object instanceof Double) {
            out.writeByte(6);
            out.writeDouble((Double)object);
        } else if (object instanceof Character) {
            out.writeByte(7);
            out.writeChar((Character)object);
        } else if (object instanceof Boolean) {
            out.writeByte(8);
            out.writeBoolean((Boolean)object);
        } else if (object instanceof String) {
            out.writeByte(9);
            out.writeUTF((String)object);
        } else if (object instanceof UUID) {
            out.writeByte(10);
            writeUUID(out, (UUID)object);
        } else if (object instanceof InetAddress) {
            out.writeByte(11);
            writeInetAddress(out, (InetAddress)object);
        } else if (object instanceof ByteStorable) {
            out.writeByte(12);
            writeByteStorable(out, (ByteStorable)object);
        } else if (object instanceof Serializable) {
            out.writeByte(13);
            writeObject(out, object);
        } else if (object instanceof GlobalPlayer) {
            out.writeByte(14);
            writeUUID(out, ((GlobalPlayer)object).getUniqueId());
        } else if (object instanceof List<?>) {
            out.writeByte(15);
            writeList(out, (List<?>)object);
        } else if (object instanceof Set<?>) {
            out.writeByte(16);
            writeSet(out, (Set<?>)object);
        } else if (object instanceof Map<?,?>) {
            out.writeByte(17);
            writeMap(out, (Map<?,?>)object);
        } else {
            throw new IllegalArgumentException("Unable to serialize value " + object);
        }
    }
    
    public static Object readTyped(DataInput in) throws IOException, ClassNotFoundException {
        int type = in.readUnsignedByte();
        
        switch (type) {
        case 0:
            return null;
        case 1:
            return in.readByte();
        case 2:
            return in.readShort();
        case 3:
            return in.readInt();
        case 4:
            return in.readLong();
        case 5:
            return in.readFloat();
        case 6:
            return in.readDouble();
        case 7:
            return in.readChar();
        case 8:
            return in.readBoolean();
        case 9:
            return in.readUTF();
        case 10:
            return readUUID(in);
        case 11:
            return readInetAddress(in);
        case 12:
            return readByteStorable(in);
        case 13:
            return readObject(in);
        case 14:
            return Global.getOfflinePlayer(readUUID(in));
        case 15:
            return readList(in);
        case 16:
            return readSet(in);
        case 17:
            return readMap(in);
        default:
            throw new IllegalStateException("Unknown data value with id " + type);
        }
    }
    
    public static boolean isSendible(Class<?> type) {
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return true;
        }
        
        if (InetAddress.class.isAssignableFrom(type)) {
            return true;
        } else if (UUID.class.equals(type)) {
            return true;
        } else if (Serializable.class.isAssignableFrom(type)) {
            return true;
        } else if (GlobalPlayer.class.isAssignableFrom(type)) {
            return true;
        } else if (List.class.isAssignableFrom(type)) {
            return true;
        } else if (Set.class.isAssignableFrom(type)) {
            return true;
        } else if (Map.class.isAssignableFrom(type)) {
            return true;
        }
        
        return false;
    }
    
    public static void writeByteStorable(DataOutput out, ByteStorable storable) throws IOException {
        out.writeUTF(storable.getClass().getName());
        storable.save(out);
    }
    
    public static ByteStorable readByteStorable(DataInput in) throws IOException, ClassNotFoundException {
        String className = in.readUTF();
        Class<? extends ByteStorable> clazz = Class.forName(className).asSubclass(ByteStorable.class);
        
        try {
            Constructor<? extends ByteStorable> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            ByteStorable storable = constructor.newInstance();
            storable.load(in);
            return storable;
        } catch (NoSuchMethodException e) {
            throw new IOException("Unable to load " + className + " because it has no default constructor");
        } catch (IllegalAccessException e) {
            throw new IOException("Unable to load " + className, e);
        } catch (InvocationTargetException e) {
            throw new IOException("Unable to load " + className, e);
        } catch (InstantiationException e) {
            throw new IOException("Unable to load " + className, e);
        }
    }
    
    public static void writeList(DataOutput out, List<?> list) throws IOException {
        out.writeInt(list.size());
        for (Object val : list) {
            writeTyped(out, val);
        }
    }
    
    public static List<?> readList(DataInput in) throws IOException, ClassNotFoundException {
        int count = in.readInt();
        List<Object> values = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; ++i) {
            values.add(readTyped(in));
        }
        
        return values;
    }
    
    public static void writeSet(DataOutput out, Set<?> set) throws IOException {
        out.writeInt(set.size());
        for (Object val : set) {
            writeTyped(out, val);
        }
    }
    
    public static Set<?> readSet(DataInput in) throws IOException, ClassNotFoundException {
        int count = in.readInt();
        Set<Object> values = Sets.newHashSetWithExpectedSize(count);
        for (int i = 0; i < count; ++i) {
            values.add(readTyped(in));
        }
        
        return values;
    }
    
    public static void writeMap(DataOutput out, Map<?, ?> map) throws IOException {
        out.writeInt(map.size());
        for (Entry<?, ?> entry : map.entrySet()) {
            writeTyped(out, entry.getKey());
            writeTyped(out, entry.getValue());
        }
    }
    
    public static Map<?, ?> readMap(DataInput in) throws IOException, ClassNotFoundException {
        int count = in.readInt();
        Map<Object, Object> values = Maps.newLinkedHashMap();
        
        for (int i = 0; i < count; ++i) {
            values.put(readTyped(in), readTyped(in));
        }
        
        return values;
    }
}
