package net.cubespace.geSuit.objects;

import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;

public class WarnInfo {
    private GlobalPlayer who;
    private long date;
    private long expireDate;
    private String reason;
    private String by;
    private UUID byId;
    
    public WarnInfo(GlobalPlayer who, String reason, String by, UUID byId, long date, long expireDate) {
        this.who = who;
        this.reason = reason;
        this.by = by;
        this.byId = byId;
        this.date = date;
        this.expireDate = expireDate;
    }
    
    public GlobalPlayer getWho() {
        return who;
    }
    
    public long getDate() {
        return date;
    }
    
    public long getExpireDate() {
        return expireDate;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getBy() {
        return by;
    }
    
    public UUID getById() {
        return byId;
    }
}
