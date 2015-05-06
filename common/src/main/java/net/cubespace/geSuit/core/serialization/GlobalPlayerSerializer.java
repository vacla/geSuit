package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.util.NetworkUtils;

class GlobalPlayerSerializer extends AdvancedSerializer<GlobalPlayer> {
    public GlobalPlayerSerializer() {
        super(TypeToken.of(GlobalPlayer.class));
    }

    @Override
    public boolean isSerializable() {
        return true;
    }

    @Override
    public void serialize(GlobalPlayer object, DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, object.getUniqueId());
    }

    @Override
    public GlobalPlayer deserialize(DataInput in) throws IOException {
        return Global.getOfflinePlayer(NetworkUtils.readUUID(in));
    }
}
