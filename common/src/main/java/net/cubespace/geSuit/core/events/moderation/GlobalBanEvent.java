package net.cubespace.geSuit.core.events.moderation;

import java.net.InetAddress;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.BanInfo;

public class GlobalBanEvent extends GSEvent {
    private BanInfo<?> ban;
    private boolean isAutomatic;
    
    private boolean includesIPBan;
    private InetAddress ip;
    public GlobalBanEvent(BanInfo<?> ban, boolean isAuto) {
        this.ban = ban;
        this.isAutomatic = isAuto;
    }
    
    public GlobalBanEvent(BanInfo<GlobalPlayer> ban, InetAddress ip, boolean isAuto) {
        this(ban, isAuto);
        
        this.ip = ip;
        this.includesIPBan = true;
        
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
        if (ban.getWho() instanceof InetAddress) {
            return (InetAddress)ban.getWho();
        } else if (includesIPBan) {
            return ip;
        } else {
            return null;
        }
    }
    
    public boolean isPlayerBan() {
        return ban.getWho() instanceof GlobalPlayer;
    }
    
    public boolean isIPBan() {
        return ban.getWho() instanceof InetAddress || includesIPBan;
    }
    
    public boolean isAutomatic() {
        return isAutomatic;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalBanEvent.class);
    }
}
