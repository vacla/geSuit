package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;

/**
 * This class represents a warning on a player
 */
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

    /**
     * @return Returns the player that was warned
     */
    public GlobalPlayer getWho() {
        return who;
    }
    
    /**
     * @return Returns the UNIX datetime in ms when they were warned
     */
    public long getDate() {
        return date;
    }
    
    /**
     * @return Returns the UNIX datetime in ms when this warning will expire
     */
    public long getExpireDate() {
        return expireDate;
    }
    
    /**
     * @return Returns the reason of the warning
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * @return Returns the name of who warned this player
     */
    public String getBy() {
        return by;
    }
    
    /**
     * @return Returns the UUID of who warned this player, or null if a non-player did
     */
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
