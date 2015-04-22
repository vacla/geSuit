package net.cubespace.geSuit.core.events.moderation;

import java.net.InetAddress;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.BanInfo;

public class GlobalUnbanEvent extends GSEvent {
    private BanInfo<?> ban;
    public GlobalUnbanEvent(BanInfo<?> ban) {
        this.ban = ban;
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
        if (isIPBan()) {
            return (InetAddress)ban.getWho();
        } else {
            return null;
        }
    }
    
    public boolean isPlayerBan() {
        return ban.getWho() instanceof GlobalPlayer;
    }
    
    public boolean isIPBan() {
        return ban.getWho() instanceof InetAddress;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalUnbanEvent.class);
    }
}
