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
    
    public T getWho() {
        return who;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public long getUntil() {
        return until;
    }
    
    public void setUntil(long date) {
        Preconditions.checkArgument(date == 0 || date > this.date);
        
        until = date;
    }
    
    public boolean isTemporary() {
        return until != 0;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getBannedBy() {
        return bannedBy;
    }
    
    public UUID getBannedById() {
        return bannedById;
    }
    
    public void setBannedBy(String name, UUID id) {
        bannedBy = name;
        bannedById = id;
    }
    
    public boolean isUnban() {
        return isUnban;
    }
    
    public void setIsUnban(boolean unban) {
        isUnban = unban;
    }
    
    public int getDatabaseKey() {
        return databaseKey;
    }
    
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
