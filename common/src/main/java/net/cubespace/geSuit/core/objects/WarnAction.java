package net.cubespace.geSuit.core.objects;

public class WarnAction {
    private ActionType type;
    private long time;
    
    public WarnAction(ActionType type, long time) {
        this.type = type;
        this.time = time;
    }
    
    public WarnAction(ActionType type) {
        this.type = type;
    }
    
    public ActionType getType() {
        return type;
    }
    
    public long getTime() {
        return time;
    }
    
    public enum ActionType {
        None,
        Mute,
        Kick,
        Ban,
        TempBan,
        IPBan,
        TempIPBan
    }
}