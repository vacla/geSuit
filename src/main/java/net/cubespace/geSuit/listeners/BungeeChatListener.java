package net.cubespace.geSuit.listeners;

import au.com.addstar.bc.event.BCPlayerJoinEvent;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeChatListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoinMessage(BCPlayerJoinEvent event) {
        GSPlayer player = PlayerManager.getPlayer(event.getPlayer());
        if (player != null) {
            // No join message for new players, alternate message is used
            if (player.isFirstJoin()) {
                event.setJoinMessage(null);
            }
            // TODO: Name change alert
        }
    }
}
