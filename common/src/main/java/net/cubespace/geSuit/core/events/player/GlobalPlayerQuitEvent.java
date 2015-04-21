package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;

public class GlobalPlayerQuitEvent extends GlobalPlayerEvent {
    public GlobalPlayerQuitEvent(GlobalPlayer player) {
        super(player);
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalPlayerQuitEvent.class);
    }
}
