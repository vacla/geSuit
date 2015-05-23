package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class UpdateBackMessage extends BaseMessage {
    public UUID player;
    public boolean isDeath;
    public Location location;
    
    public UpdateBackMessage() {}
    public UpdateBackMessage(UUID player, boolean isDeath, Location location) {
        this.player = player;
        this.isDeath = isDeath;
        this.location = location;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, player);
        out.writeBoolean(isDeath);
        out.writeUTF(location.toSerialized());
    }

    @Override
    public void read(DataInput in) throws IOException {
        player = NetworkUtils.readUUID(in);
        isDeath = in.readBoolean();
        location = Location.fromSerialized(in.readUTF());
    }

}
