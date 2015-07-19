package net.cubespace.geSuit.events;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.general.BroadcastManager;

/**
 * Called upon the player joining to add notifications.
 * The notifications can be globally, to a broadcast group,
 * or privately to the player.
 */
public class JoinNotificationsEvent extends GSEvent {
    private final GlobalPlayer player;
    
    private List<String> globals;
    private List<String> privates;
    
    private Multimap<String, String> groups;
    
    public JoinNotificationsEvent(GlobalPlayer player) {
        this.player = player;
        
        globals = Lists.newArrayList();
        privates = Lists.newArrayList();
        groups = LinkedListMultimap.create();
    }
    
    /**
     * @return Returns the player to add notifications for
     */
    public GlobalPlayer getPlayer() {
        return player;
    }
    
    /**
     * Adds a broadcast that will be displayed to all players
     * and the console.
     * @param message The message to broadcast
     */
    public void addBroadcastNotification(String message) {
        globals.add(message);
    }
    
    /**
     * Adds a broadcast that will be displayed to players
     * in the specified broadcast group.
     * @param message The message to broadcast
     * @param group The broadcast group, same as in {@link BroadcastManager#broadcastGroup(String, String)}
     */
    public void addGroupNotification(String message, String group) {
        groups.put(group, message);
    }
    
    /**
     * Adds a message that will be displayed to this player
     * only.
     * @param message The message to send
     */
    public void addPrivateNotification(String message) {
        privates.add(message);
    }
    
    public List<String> getGlobalMessages() {
        return globals;
    }
    
    public List<String> getPrivateMessages() {
        return privates;
    }
    
    public Collection<Entry<String, String>> getGroupMessages() {
        return groups.entries();
    }
}
