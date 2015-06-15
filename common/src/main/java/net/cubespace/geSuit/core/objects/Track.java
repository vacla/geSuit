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
    private int nameBanned;
    private int ipBanned;

    public Track(String name, String nickname, UUID uuid, InetAddress ip, long firstseen, long lastseen, int nameBanned, int ipBanned) {
        this.name = name;
        this.uuid = uuid;
        this.ip = ip;
        this.firstseen = firstseen;
        this.lastseen = lastseen;
        this.nameBanned = nameBanned;
        this.ipBanned = ipBanned;
    }
    
    public Track() {}

    public String getName() {
        return name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getDisplayName() {
        if (nickname != null) {
            return nickname;
        } else {
            return name;
        }
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
        return ipBanned != 0;
    }
    
    public boolean isIpBanTemp() {
        return ipBanned == 2;
    }
    
    public boolean isNameBanned() {
        return nameBanned != 0;
    }
    
    public boolean isNameBanTemp() {
        return nameBanned == 2;
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
        
        out.writeByte(nameBanned | (ipBanned << 4));
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
        
        int val = in.readByte();
        ipBanned = (val >> 4) & 15;
        nameBanned = val & 15;
    }
}