package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;

public abstract class GlobalPlayerEvent extends GSEvent {
    private GlobalPlayer player;
    
    protected GlobalPlayerEvent(GlobalPlayer player) {
        this.player = player;
    }
    
    public final GlobalPlayer getPlayer() {
        return player;
    }
}
