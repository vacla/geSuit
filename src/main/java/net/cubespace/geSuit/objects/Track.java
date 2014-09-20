package net.cubespace.geSuit.objects;

import java.sql.Timestamp;
import java.util.Date;

public class Track {
    private String player;
    private String uuid;
    private String ip;
    private Date firstseen;
    private Date lastseen;

    public Track(String player, String uuid, String ip, Timestamp firstseen, Timestamp lastseen) {
        this.player = player;
        this.uuid = uuid;
        this.ip = ip;
        this.firstseen = firstseen;
        this.lastseen = lastseen;
    }

    public String getPlayer() {
        return player;
    }

    public Date getFirstSeen() {
        return firstseen;
    }

    public Date getLastSeen() {
        return lastseen;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getIp()
    {
        return ip;
    }
}