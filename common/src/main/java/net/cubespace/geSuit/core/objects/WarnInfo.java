package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class WarnInfo implements ByteStorable {
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
    
    public WarnInfo() {}
    
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
    
    @Override
    public void load(DataInput in) throws IOException {
        who = Global.getOfflinePlayer(NetworkUtils.readUUID(in));
        date = in.readLong();
        expireDate = in.readLong();
        reason = in.readUTF();
        by = in.readUTF();
        if (in.readBoolean()) {
            byId = NetworkUtils.readUUID(in);
        }
    }
    
    @Override
    public void save(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, who.getUniqueId());
        out.writeLong(date);
        out.writeLong(expireDate);
        out.writeUTF(reason);
        out.writeUTF(by);
        if (byId != null) {
            out.writeBoolean(true);
            NetworkUtils.writeUUID(out, byId);
        } else {
            out.writeBoolean(false);
        }
    }
}
