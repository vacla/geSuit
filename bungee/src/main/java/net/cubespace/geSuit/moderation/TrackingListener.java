package net.cubespace.geSuit.moderation;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerQuitEvent;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.events.GlobalPlayerJoinMessageEvent;
import net.cubespace.geSuit.events.JoinNotificationsEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TrackingListener implements Listener {
    private TrackingManager trackingManager;
    private Messages messages;
    private Logger logger;
    
    public TrackingListener(TrackingManager trackingManager, Messages messages, Logger logger) {
        this.trackingManager = trackingManager;
        this.messages = messages;
        this.logger = logger;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(GlobalPlayerJoinMessageEvent event) {
        trackingManager.updateTracking(event.getPlayer());
        
        if (event.getPlayer().isNewPlayer()) {
            return;
        }
        
        // We dont need to display name change join message
        // if there isnt to be a message anyway
        if (event.getMessage() == null) {
            return;
        }
        
        // Name change
        Track previousName = trackingManager.checkNameChange(event.getPlayer());
        if (previousName != null) {
            event.setMessage(messages.get(
                    "connect.join.namechange",
                    "player", event.getPlayer().getDisplayName(),
                    "old", previousName.getDisplayName()));
            logger.info(messages.get(
                  "connect.join.namechange.log",
                  "player", event.getPlayer().getDisplayName(),
                  "old", previousName.getName()));
        }
    }
    
    @EventHandler
    public void onPlayerNotifications(JoinNotificationsEvent event) {
        trackingManager.addPlayerInfo(event);
    }
    
    @EventHandler
    public void onNickname(GlobalPlayerNicknameEvent event) {
        trackingManager.updateTracking(event.getPlayer());
    }
    
    @EventHandler
    public void onQuit(GlobalPlayerQuitEvent event) {
        // Update time tracking (if enabled)
        if (trackingManager.shouldTrackOntime()) {
            trackingManager.updatePlayerOnTime(event.getPlayer());
        }
    }
}
