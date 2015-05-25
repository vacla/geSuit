package net.cubespace.geSuit.core.events.moderation;

import java.net.InetAddress;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.BanInfo;

/**
 * This event is called upon the unbanning of a player or ip
 */
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
    
    /**
     * @return Returns the ban information
     */
    public BanInfo<?> getBan() {
        return ban;
    }
    
    /**
     * @return Returns the player being unbanned in the case of name bans. Plain IP bans do not include a player
     */
    public GlobalPlayer getPlayer() {
        if (isPlayerBan()) {
            return (GlobalPlayer)ban.getWho();
        } else {
            return null;
        }
    }
    
    /**
     * @return Returns the IP address being unbanned. If this is a name only ban then this will be null
     */
    public InetAddress getAddress() {
        if (address != null) {
            return address;
        } else if (ban.getWho() instanceof InetAddress) {
            return (InetAddress)ban.getWho();
        } else {
            return null;
        }
    }
    
    /**
     * @return Returns true if a player is being unbanned
     */
    public boolean isPlayerBan() {
        return ban.getWho() instanceof GlobalPlayer;
    }
    
    /**
     * @return Returns true if an IP is being unbanned
     */
    public boolean isIPBan() {
        return ban.getWho() instanceof InetAddress || address != null;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalUnbanEvent.class);
    }
}
