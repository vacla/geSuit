package net.cubespace.geSuit.core;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.events.GSEvent;

public interface Platform {
    public void callEvent(GSEvent event);
    
    public Logger getLogger();
}
