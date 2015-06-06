package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class TeleportMessage extends BaseMessage {
    public UUID player;
    public Location targetLocation;
    public UUID targetPlayer;
    public String targetServer;
    public int cause;
    public boolean safe;
    
    public TeleportMessage() {}
    public TeleportMessage(UUID player, Location target, int cause, boolean safe) {
        this.player = player;
        this.targetLocation = target;
        this.cause = cause;
        this.safe = safe;
    }
    
    public TeleportMessage(UUID player, UUID target, int cause, boolean safe) {
        this.player = player;
        this.targetPlayer = target;
        this.cause = cause;
        this.safe = safe;
    }
    
    public TeleportMessage(UUID player, String server, int cause, boolean safe) {
        this.player = player;
        this.targetServer = server;
        this.cause = cause;
        this.safe = safe;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, player);
        out.writeByte(cause);
        out.writeBoolean(safe);
        
        if (targetLocation != null) {
            out.writeByte(0);
            out.writeUTF(targetLocation.save());
        } else if (targetPlayer != null) {
            out.writeByte(1);
            NetworkUtils.writeUUID(out, targetPlayer);
        } else {
            out.writeByte(2);
            out.writeUTF(targetServer);
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        player = NetworkUtils.readUUID(in);
        cause = in.readUnsignedByte();
        safe = in.readBoolean();
        
        switch (in.readByte()) {
        case 0: // Location
            targetLocation = new Location();
            targetLocation.load(in.readUTF());
            break;
        case 1: // Player
            targetPlayer = NetworkUtils.readUUID(in);
            break;
        case 2: // Server
            targetServer = in.readUTF();
            break;
        }
    }
}
