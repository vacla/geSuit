package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;

/**
 * This event is called upon a player joining the proxy.
 * More specifically, this is called upon successfully connecting to a server for the first time since they joined the proxy. 
 */
public class GlobalPlayerJoinEvent extends GlobalPlayerEvent {
    public GlobalPlayerJoinEvent(GlobalPlayer player) {
        super(player);
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalPlayerJoinEvent.class);
    }
}
