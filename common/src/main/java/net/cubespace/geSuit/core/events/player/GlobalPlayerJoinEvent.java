package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;

public class GlobalPlayerJoinEvent extends GlobalPlayerEvent {
    public GlobalPlayerJoinEvent(GlobalPlayer player) {
        super(player);
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalPlayerJoinEvent.class);
    }
}
