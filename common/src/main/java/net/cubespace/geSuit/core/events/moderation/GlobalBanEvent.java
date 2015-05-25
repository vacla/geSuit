package net.cubespace.geSuit.core.events.moderation;

import java.net.InetAddress;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.BanInfo;

/**
 * This event is called upon a player or ip being banned.
 */
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
    
    /**
     * @return Returns the ban information
     */
    public BanInfo<?> getBan() {
        return ban;
    }
    
    /**
     * @return Returns the player being banned in the case of name bans. Plain IP bans do not include a player
     */
    public GlobalPlayer getPlayer() {
        if (isPlayerBan()) {
            return (GlobalPlayer)ban.getWho();
        } else {
            return null;
        }
    }
    
    /**
     * @return Returns the IP address being banned. If this is a name only ban then this will be null
     */
    public InetAddress getAddress() {
        if (ban.getWho() instanceof InetAddress) {
            return (InetAddress)ban.getWho();
        } else if (includesIPBan) {
            return ip;
        } else {
            return null;
        }
    }
    
    /**
     * @return Returns true if a player is being banned
     */
    public boolean isPlayerBan() {
        return ban.getWho() instanceof GlobalPlayer;
    }
    
    /**
     * @return Returns true if an IP is being banned
     */
    public boolean isIPBan() {
        return ban.getWho() instanceof InetAddress || includesIPBan;
    }
    
    /**
     * @return Returns true if this ban is automatic. This is usually the case if a warn triggers this ban
     */
    public boolean isAutomatic() {
        return isAutomatic;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalBanEvent.class);
    }
}
