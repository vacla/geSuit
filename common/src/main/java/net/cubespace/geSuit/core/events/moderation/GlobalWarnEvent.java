package net.cubespace.geSuit.core.events.moderation;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.objects.WarnAction;
import net.cubespace.geSuit.core.objects.WarnInfo;

public class GlobalWarnEvent extends GSEvent {
    private WarnInfo warn;
    private WarnAction action;
    private int number;
    
    public GlobalWarnEvent(WarnInfo warn, WarnAction action, int number) {
        this.warn = warn;
        this.action = action;
        this.number = number;
    }
    
    public GlobalPlayer getPlayer() {
        return warn.getWho();
    }
    
    public WarnInfo getWarning() {
        return warn;
    }
    
    public WarnAction getAction() {
        return action;
    }
    
    public int getNumber() {
        return number;
    }
}
