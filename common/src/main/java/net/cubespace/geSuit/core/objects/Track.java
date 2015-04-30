package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;
import net.cubespace.geSuit.core.util.Utilities;

public class Track implements ByteStorable {
    private String name;
    private String nickname;
    private UUID uuid;
    private InetAddress ip;
    private long firstseen;
    private long lastseen;
    private boolean isNameBanned;
    private boolean isIPBanned;

    public Track(String name, String nickname, UUID uuid, InetAddress ip, long firstseen, long lastseen, boolean isNameBanned, boolean isIPBanned) {
        this.name = name;
        this.uuid = uuid;
        this.ip = ip;
        this.firstseen = firstseen;
        this.lastseen = lastseen;
        this.isNameBanned = isNameBanned;
        this.isIPBanned = isIPBanned;
    }
    
    public Track() {}

    public String getName() {
        return name;
    }
    
    public String getNickname() {
        return nickname;
    }

    public long getFirstSeen() {
        return firstseen;
    }

    public long getLastSeen() {
        return lastseen;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public InetAddress getIp() {
        return ip;
    }
    
    public boolean isIpBanned() {
        return isIPBanned;
    }
    
    public boolean isNameBanned() {
        return isNameBanned;
    }
    
    @Override
    public String toString() {
        return String.format("name: %s nickname: %s uuid: %s ip: %s date: %s", name, nickname, uuid, ip, Utilities.formatDate(lastseen));
    }
    
    @Override
    public void save(DataOutput out) throws IOException {
        out.writeUTF(name);
        if (nickname != null) {
            out.writeBoolean(true);
            out.writeUTF(nickname);
        } else {
            out.writeBoolean(false);
        }
        
        NetworkUtils.writeUUID(out, uuid);
        NetworkUtils.writeInetAddress(out, ip);
        
        out.writeLong(firstseen);
        out.writeLong(lastseen);
        
        out.writeBoolean(isNameBanned);
        out.writeBoolean(isIPBanned);
    }
    
    @Override
    public void load(DataInput in) throws IOException {
        name = in.readUTF();
        if (in.readBoolean()) {
            nickname = in.readUTF();
        }
        
        uuid = NetworkUtils.readUUID(in);
        ip = NetworkUtils.readInetAddress(in);
        
        firstseen = in.readLong();
        lastseen = in.readLong();
        
        isNameBanned = in.readBoolean();
        isIPBanned = in.readBoolean();
    }
}