package net.cubespace.geSuit.core.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

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
                return in.readByte();
            }
        });

        Object object = in2.readObject();
        in2.close();

        return object;
    }
}
