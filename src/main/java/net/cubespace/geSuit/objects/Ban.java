package net.cubespace.geSuit.objects;

import java.sql.Timestamp;
import java.util.Date;

public class Ban {
    private int id;
    private String player;
    private String bannedBy;
    private String reason;
    private String type;
    private Date bannedOn;
    private Date bannedUntil;

    public Ban(int id, String player, String bannedBy, String reason, String type, Timestamp timestamp, Timestamp timestamp2) {
        this.id = id;
        this.player = player;
        this.bannedBy = bannedBy;
        this.reason = reason;
        this.type = type;
        this.bannedOn = timestamp;
        this.bannedUntil = timestamp2;
    }

    public String getPlayer() {
        return player;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public String getReason() {
        return reason;
    }

    public String getType() {
        return type;
    }

    public Date getBannedOn() {
        return bannedOn;
    }

    public Date getBannedUntil() {
        return bannedUntil;
    }

    public int getId() {
        return id;
    }
}
