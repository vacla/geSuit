package net.cubespace.geSuit.core.events;

import net.cubespace.geSuit.core.GlobalServer;

/**
 * This event is called upon the state of the network being updated.
 * Plugins needing state information should reload it upon
 * handling this event.
 */
public class GlobalReloadEvent extends GSEvent {
    private final GlobalServer thisServer;
    
    public GlobalReloadEvent(GlobalServer server) {
        thisServer = server;
    }
    
    public GlobalServer getCurrentServer() {
        return thisServer;
    }
    
    public static Object getHandlerList() {
        return getHandlerList(GlobalReloadEvent.class);
    }
}
