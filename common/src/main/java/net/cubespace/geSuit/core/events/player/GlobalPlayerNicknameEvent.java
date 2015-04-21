package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;

public class GlobalPlayerNicknameEvent extends GlobalPlayerEvent {
    private String previous;
    
    public GlobalPlayerNicknameEvent(GlobalPlayer player, String previous) {
        super(player);
        
        this.previous = previous;
    }
    
    public String getCurrentName() {
        return getPlayer().getNickname();
    }
    
    public String getPreviousName() {
        return previous;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalPlayerNicknameEvent.class);
    }
}
