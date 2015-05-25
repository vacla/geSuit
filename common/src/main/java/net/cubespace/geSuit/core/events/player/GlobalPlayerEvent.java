package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;

/**
 * This class is a base for all player based events
 */
public abstract class GlobalPlayerEvent extends GSEvent {
    private GlobalPlayer player;
    
    protected GlobalPlayerEvent(GlobalPlayer player) {
        this.player = player;
    }
    
    /**
     * @return Returns the player this event is about
     */
    public final GlobalPlayer getPlayer() {
        return player;
    }
}
