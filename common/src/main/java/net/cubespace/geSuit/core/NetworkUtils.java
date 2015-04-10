package net.cubespace.geSuit.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class NetworkUtils {
    public static void writeUUID(DataOutput out, UUID uuid) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }
    
    public static UUID readUUID(DataInput in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
}
