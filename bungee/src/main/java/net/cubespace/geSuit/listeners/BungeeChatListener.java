package net.cubespace.geSuit.listeners;

import au.com.addstar.bc.event.BCPlayerJoinEvent;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Track;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public class BungeeChatListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoinMessage(BCPlayerJoinEvent event) {
        GSPlayer player = PlayerManager.getPlayer(event.getPlayer());
        if (player != null) {
            // No join message for new players, alternate message is used
            if (player.isFirstJoin()) {
                event.setJoinMessage(null);
            } else if (player.getLastName() != null) {
                Track lastName = player.getLastName();
                
                // Display the last name if it changed less than the config value days ago
                if (System.currentTimeMillis() - lastName.getLastSeen().getTime() < TimeUnit.DAYS.toMillis(ConfigManager.bans.NameChangeNotifyTime)) {
                	// Always log recent name changes to console
                	LoggingManager.log(ChatColor.translateAlternateColorCodes('&', ConfigManager.messages.PLAYER_JOIN_NAMECHANGE_PROXY
                			.replace("{player}", event.getPlayer().getDisplayName())
                			.replace("{old}", lastName.getPlayer())));

                    // Do not show this for nicknamed players (usually the case that the previous name was rude or inappropriate)
                    if (event.getPlayer().getName().equals(event.getPlayer().getDisplayName())) {
                        event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', ConfigManager.messages.PLAYER_JOIN_NAMECHANGE.replace("{player}", event.getPlayer().getDisplayName()).replace("{old}", lastName.getPlayer())));
                    }
                }
            }
        }
    }
}
