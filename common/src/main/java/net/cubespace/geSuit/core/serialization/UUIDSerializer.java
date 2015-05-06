package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.util.NetworkUtils;

class UUIDSerializer extends AdvancedSerializer<UUID> {
    public UUIDSerializer() {
        super(TypeToken.of(UUID.class));
    }

    @Override
    public boolean isSerializable() {
        return true;
    }

    @Override
    public void serialize(UUID object, DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, object);
    }

    @Override
    public UUID deserialize(DataInput in) throws IOException {
        return NetworkUtils.readUUID(in);
    }
}
