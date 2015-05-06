package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;

import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.util.NetworkUtils;

class InetAddressSerializer extends AdvancedSerializer<InetAddress> {
    public InetAddressSerializer() {
        super(TypeToken.of(InetAddress.class));
    }

    @Override
    public boolean isSerializable() {
        return true;
    }

    @Override
    public void serialize(InetAddress object, DataOutput out) throws IOException {
        NetworkUtils.writeInetAddress(out, object);
    }

    @Override
    public InetAddress deserialize(DataInput in) throws IOException {
        return NetworkUtils.readInetAddress(in);
    }
}
