package net.cubespace.geSuit.events;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;

public class GlobalPlayerJoinMessageEvent extends GSEvent {
    private GlobalPlayer player;
    private String message;
    
    public GlobalPlayerJoinMessageEvent(GlobalPlayer player, String message) {
        this.player = player;
        this.message = message;
    }
    
    /**
     * @return Returns the player who this message is for
     */
    public GlobalPlayer getPlayer() {
        return player;
    }
    
    /**
     * @return Returns the join message. Can be null if nothing will be broadcast
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message displayed to everyone on join.
     * @param message The message to display or null to hide it
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
