package net.cubespace.geSuit.core;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.events.GSEvent;

/**
 * This class contains some methods whose result or action depends on the platform this is run on.
 */
public interface Platform {
    /**
     * Raises a GSEvent with the appropriate event system for this platform
     * @param event The event to raise
     */
    public void callEvent(GSEvent event);
    
    /**
     * @return Returns the Logger for this platform
     */
    public Logger getLogger();
}
