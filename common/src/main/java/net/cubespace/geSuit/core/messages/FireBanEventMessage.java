package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class FireBanEventMessage extends BaseMessage {
    public BanInfo<?> ban;
    public InetAddress address;
    public boolean auto;
    public boolean isUnban;
    
    public FireBanEventMessage() {}
    
    private FireBanEventMessage(BanInfo<?> ban, InetAddress address, boolean auto, boolean isUnban) {
        this.ban = ban;
        this.auto = auto;
        this.address = address;
        this.isUnban = isUnban;
    }
    
    public static FireBanEventMessage createFromBan(BanInfo<?> ban, boolean auto) {
        return new FireBanEventMessage(ban, null, auto, false);
    }
    
    public static FireBanEventMessage createFromIPBan(BanInfo<GlobalPlayer> ban, InetAddress address, boolean auto) {
        return new FireBanEventMessage(ban, address, auto, false);
    }
    
    public static FireBanEventMessage createFromUnban(BanInfo<?> ban) {
        return new FireBanEventMessage(ban, null, false, true);
    }
    
    public static FireBanEventMessage createFromIPUnban(BanInfo<GlobalPlayer> ban, InetAddress address) {
        return new FireBanEventMessage(ban, address, false, true);
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeBoolean(auto);
        out.writeBoolean(ban.isUnban());
        if (ban.getWho() instanceof GlobalPlayer) {
            out.writeByte(0);
            NetworkUtils.writeUUID(out, ((GlobalPlayer)ban.getWho()).getUniqueId());
        } else if (ban.getWho() instanceof InetAddress) {
            out.writeByte(1);
            NetworkUtils.writeInetAddress(out, (InetAddress)ban.getWho());
        } else {
            throw new AssertionError("Unknown ban who type");
        }
        
        out.writeInt(ban.getDatabaseKey());
        out.writeLong(ban.getDate());
        out.writeLong(ban.getUntil());
        out.writeUTF(ban.getReason());
        
        out.writeUTF(ban.getBannedBy());
        if (ban.getBannedById() != null) {
            out.writeBoolean(true);
            NetworkUtils.writeUUID(out, ban.getBannedById());
        } else {
            out.writeBoolean(false);
        }
        
        if (address != null) {
            out.writeBoolean(true);
            NetworkUtils.writeInetAddress(out, address);
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        auto = in.readBoolean();
        boolean isUnban = in.readBoolean();
        switch (in.readByte()) {
        case 0: // player
            ban = new BanInfo<GlobalPlayer>(Global.getOfflinePlayer(NetworkUtils.readUUID(in)));
            break;
        case 1: // IP
            ban = new BanInfo<InetAddress>(NetworkUtils.readInetAddress(in));
            break;
        default:
            throw new IOException("Unknown ban who type");
        }
        
        ban.setIsUnban(isUnban);
        ban.setDatabaseKey(in.readInt());
        ban.setDate(in.readLong());
        ban.setUntil(in.readLong());
        ban.setReason(in.readUTF());
        ban.setBannedBy(in.readUTF(), (in.readBoolean() ? NetworkUtils.readUUID(in) : null));
        
        if (in.readBoolean()) {
            address = NetworkUtils.readInetAddress(in);
        }
    }

}
