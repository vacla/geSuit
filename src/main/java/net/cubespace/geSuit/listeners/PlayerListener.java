package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogin(ServerConnectedEvent e) throws SQLException {
    	GSPlayer p = PlayerManager.getPlayer(e.getPlayer().getName());
    	if (p == null) {
    		// Player is not already known to be "online" - Grab their profile
    		p = PlayerManager.loadPlayer(e.getPlayer());

	        if (ConfigManager.main.MOTD_Enabled && p.firstConnect()) {
	            if (p.isFirstJoin()) {
	        	PlayerManager.sendMessageToTarget(e.getPlayer().getName(), ConfigManager.motdNew.getMOTD().replace("{player}", p.getName()));
	            } else {
	        	PlayerManager.sendMessageToTarget(e.getPlayer().getName(), ConfigManager.motd.getMOTD().replace("{player}", p.getName()));
	            }
	        }

        	p.connected();
    	} else {
    		if (p.isNewSpawn()) {
    			// Send player to new spawn location if their player record has the "newspawn" flag
    			SpawnManager.sendPlayerToNewPlayerSpawn(p);
    		}
    	}

    	// Update player tracking
    	final GSPlayer fp = p;
    	geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
			@Override
			public void run() {
				PlayerManager.updateTracking(fp);
			}
    	}, 10, TimeUnit.MILLISECONDS); 
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogout(final PlayerDisconnectEvent e) {
        int dcTime = ConfigManager.main.PlayerDisconnectDelay;
        final GSPlayer p = PlayerManager.getPlayer(e.getPlayer().getName());
        if (dcTime > 0) {
            geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
                @Override
                public void run() {
                    if (!PlayerManager.kickedPlayers.contains(e.getPlayer())) {
                        if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                            PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getName()));
                        }
                    } else {
                        PlayerManager.kickedPlayers.remove(e.getPlayer());
                    }

                    PlayerManager.unloadPlayer(e.getPlayer().getName());
                }

            }, dcTime, TimeUnit.SECONDS);
        } else {
            if (!PlayerManager.kickedPlayers.contains(e.getPlayer())) {
                if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                    PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getName()));
                }
            } else {
                PlayerManager.kickedPlayers.remove(e.getPlayer());
            }

            PlayerManager.unloadPlayer(e.getPlayer().getName());
        }
    }
}
