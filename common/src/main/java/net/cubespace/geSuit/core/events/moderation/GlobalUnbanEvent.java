package net.cubespace.geSuit.core.events.moderation;

import java.net.InetAddress;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.BanInfo;

public class GlobalUnbanEvent extends GSEvent {
    private BanInfo<?> ban;
    private InetAddress address;
    
    public GlobalUnbanEvent(BanInfo<?> ban) {
        this.ban = ban;
    }
    
    public GlobalUnbanEvent(BanInfo<GlobalPlayer> ban, InetAddress address) {
        this.ban = ban;
        this.address = address;
    }
    
    public BanInfo<?> getBan() {
        return ban;
    }
    
    public GlobalPlayer getPlayer() {
        if (isPlayerBan()) {
            return (GlobalPlayer)ban.getWho();
        } else {
            return null;
        }
    }
    
    public InetAddress getAddress() {
        if (address != null) {
            return address;
        } else if (ban.getWho() instanceof InetAddress) {
            return (InetAddress)ban.getWho();
        } else {
            return null;
        }
    }
    
    public boolean isPlayerBan() {
        return ban.getWho() instanceof GlobalPlayer;
    }
    
    public boolean isIPBan() {
        return ban.getWho() instanceof InetAddress || address != null;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalUnbanEvent.class);
    }
}
