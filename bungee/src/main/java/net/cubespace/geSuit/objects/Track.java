package net.cubespace.geSuit.objects;

import java.net.InetAddress;
import java.util.UUID;

import net.cubespace.geSuit.core.util.Utilities;

public class Track {
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
}