package net.cubespace.geSuit.core.events.moderation;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.WarnAction;
import net.cubespace.geSuit.core.objects.WarnInfo;

/**
 * This event is called when a player is warned.
 */
public class GlobalWarnEvent extends GSEvent {
    private WarnInfo warn;
    private WarnAction action;
    private int number;
    
    public GlobalWarnEvent(WarnInfo warn, WarnAction action, int number) {
        this.warn = warn;
        this.action = action;
        this.number = number;
    }
    
    /**
     * @return The player being warned
     */
    public GlobalPlayer getPlayer() {
        return warn.getWho();
    }
    
    /**
     * @return The warning information including the reason, who did the warning, time and expiry date
     */
    public WarnInfo getWarning() {
        return warn;
    }
    
    /**
     * @return The action that will be executed on this player
     */
    public WarnAction getAction() {
        return action;
    }
    
    /**
     * @return The warn number. 1 based where 1 is the first
     */
    public int getNumber() {
        return number;
    }
}
