package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.storage.Storable;
import net.cubespace.geSuit.core.util.NetworkUtils;
import net.cubespace.geSuit.core.util.Utilities;

/**
 * This class represents a ban on some object. In practice, the only used types are GlobalPlayer and InetAddress 
 * @param <T> The type of the object the ban is on
 */
public class BanInfo<T> implements Storable, ByteStorable {
    private T who;
    private long date;
    private long until;
    private String reason;
    private String bannedBy;
    private UUID bannedById;
    private boolean isUnban;
    private int databaseKey = -1;
    
    public BanInfo(T who) {
        this.who = who;
    }
    
    public BanInfo(T who, int databaseKey, String reason, String byName, UUID byId, long date, long until, boolean unban) {
        this.who = who;
        this.databaseKey = databaseKey;
        this.reason = reason;
        this.date = date;
        this.until = until;
        this.bannedBy = byName;
        this.bannedById = byId;
        this.isUnban = unban;
    }
    
    protected BanInfo() {}
    
    /**
     * @return Returns the target of this ban
     */
    public T getWho() {
        return who;
    }
    
    /**
     * @return Returns the UNIX datetime in ms of when the ban occurred
     */
    public long getDate() {
        return date;
    }
    
    /**
     * Sets the datetime of the ban
     * @param date The UNIX datetime in ms
     */
    public void setDate(long date) {
        this.date = date;
    }
    
    /**
     * @return Returns the UNIX datetime in ms of when the ban expires, or 0 if there is no expiry
     */
    public long getUntil() {
        return until;
    }
    
    /**
     * Sets the datetime when this ban expires
     * @param date The UNIX datetime in ms or 0. This must be later than the ban date
     */
    public void setUntil(long date) {
        Preconditions.checkArgument(date == 0 || date > this.date);
        
        until = date;
    }
    
    /**
     * @return Returns true if this ban has an expiry date
     */
    public boolean isTemporary() {
        return until != 0;
    }
    
    /**
     * @return Returns the reason for this ban
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Sets the reason for this ban
     * @param reason The reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * @return Returns the name of the player or object that applied the ban
     */
    public String getBannedBy() {
        return bannedBy;
    }
    
    /**
     * @return Returns the UUID of the player that applied the ban. This may be null if the ban was created by a non-player
     */
    public UUID getBannedById() {
        return bannedById;
    }
    
    /**
     * Sets who applied the ban
     * @param name The name of the banner
     * @param id The UUID of the banner, or null
     */
    public void setBannedBy(String name, UUID id) {
        bannedBy = name;
        bannedById = id;
    }
    
    /**
     * @return Returns true if this is actually an unban
     */
    public boolean isUnban() {
        return isUnban;
    }
    
    /**
     * Sets whether this is an unban or not
     * @param unban True if this is an unban
     */
    public void setIsUnban(boolean unban) {
        isUnban = unban;
    }
    
    /**
     * @return Returns the database row id. This is used for ban - unban linking
     */
    public int getDatabaseKey() {
        return databaseKey;
    }
    
    /**
     * Sets the database row id. This should never be used by plugins
     */
    public void setDatabaseKey(int key) {
        databaseKey = key;
    }
    
    @Override
    public void load(Map<String, String> values) {
        databaseKey = Integer.parseInt(values.get("id"));
        date = Utilities.parseDate(values.get("date"));
        reason = values.get("reason");
        bannedBy = values.get("banned-by");
        if (values.containsKey("banned-by-id")) {
            bannedById = Utilities.makeUUID(values.get("banned-by-id"));
        } else {
            bannedById = null;
        }
        
        if (values.containsKey("until")) {
            until = Utilities.parseDate(values.get("until"));
        } else {
            until = 0;
        }
    }
    
    @Override
    public void save(Map<String, String> values) {
        values.put("id", String.valueOf(databaseKey));
        values.put("date", Utilities.formatDate(date));
        values.put("reason", reason);
        values.put("banned-by", bannedBy);
        if (bannedById != null) {
            values.put("banned-by-id", Utilities.toString(bannedById));
        }
        
        if (until != 0) {
            values.put("until", Utilities.formatDate(until));
        }
    }

    @Override
    public void save(DataOutput out) throws IOException {
        if (who instanceof GlobalPlayer) {
            out.writeByte(0);
            NetworkUtils.writeUUID(out, ((GlobalPlayer)who).getUniqueId());
        } else if (who instanceof InetAddress) {
            out.writeByte(1);
            NetworkUtils.writeInetAddress(out, (InetAddress)who);
        } else {
            throw new AssertionError("Unknown ban target type");
        }
        
        out.writeInt(databaseKey);
        out.writeLong(date);
        out.writeLong(until);
        out.writeBoolean(isUnban);
        if (reason != null) {
            out.writeBoolean(true);
            out.writeUTF(reason);
        } else {
            out.writeBoolean(false);
        }
        
        out.writeUTF(bannedBy);
        if (bannedById != null) {
            out.writeBoolean(true);
            NetworkUtils.writeUUID(out, bannedById);
        } else {
            out.writeBoolean(false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(DataInput in) throws IOException {
        switch (in.readByte()) {
        case 0:
            UUID id = NetworkUtils.readUUID(in);
            who = (T)Global.getOfflinePlayer(id);
            break;
        case 1:
            who = (T)NetworkUtils.readInetAddress(in);
            break;
        }
        
        databaseKey = in.readInt();
        date = in.readLong();
        until = in.readLong();
        isUnban = in.readBoolean();
        if (in.readBoolean()) {
            reason = in.readUTF();
        }
        bannedBy = in.readUTF();
        if (in.readBoolean()) {
            bannedById = NetworkUtils.readUUID(in);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BanInfo<?>)) {
            return false;
        }
        
        BanInfo<?> other = (BanInfo<?>)obj;
        
        return who.equals(other.who) 
                && date == other.date 
                && until == other.until 
                && reason.equals(other.reason) 
                && bannedBy.equals(other.bannedBy) 
                && ((bannedById != null && bannedById.equals(other.bannedById)) || (bannedById == null && other.bannedById == null)) 
                && isUnban == other.isUnban 
                && databaseKey == other.databaseKey;
    }
}
