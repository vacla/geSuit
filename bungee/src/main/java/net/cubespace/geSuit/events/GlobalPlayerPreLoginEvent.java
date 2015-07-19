package net.cubespace.geSuit.events;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * <p>The GlobalPlayerPreLoginEvent is called when the player is just beginning the join process of the server.
 * At this stage they are not marked as online.</p>
 * 
 * <p>This event can be used to deny the player from joining based on bans, lockouts, etc.</p>
 */
public class GlobalPlayerPreLoginEvent extends GSEvent implements Cancellable {
    private final GlobalPlayer player;
    
    private String cancelMessage;
    private boolean isCancelled;
    
    public GlobalPlayerPreLoginEvent(GlobalPlayer player) {
        this.player = player;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
    
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
    
    /**
     * @return Returns the player that is logging in
     */
    public GlobalPlayer getPlayer() {
        return player;
    }
    
    /**
     * Denies the player from logging into the network.
     * This sets both the message and the cancelled state.
     * @param message The message the player will see
     */
    public void denyLogin(String message) {
        cancelMessage = message;
        isCancelled = true;
    }
    
    /**
     * @return Returns the message that will be shown to the player when they are
     *         kicked.
     */
    public String getCancelMessage() {
        return cancelMessage;
    }
}
