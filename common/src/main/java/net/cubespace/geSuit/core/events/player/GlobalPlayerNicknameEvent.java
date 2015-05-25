package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;

/**
 * This event is called upon a players nickname changing
 */
public class GlobalPlayerNicknameEvent extends GlobalPlayerEvent {
    private String previous;
    
    public GlobalPlayerNicknameEvent(GlobalPlayer player, String previous) {
        super(player);
        
        this.previous = previous;
    }
    
    /**
     * @return Returns the current or new nickname for the player
     */
    public String getCurrentName() {
        return getPlayer().getNickname();
    }
    
    /**
     * @return Returns the nickname before the change
     */
    public String getPreviousName() {
        return previous;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalPlayerNicknameEvent.class);
    }
}
