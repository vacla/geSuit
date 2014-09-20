package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory.BungeeGroup;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogin(final ServerConnectedEvent e) throws SQLException {
    	if (PlayerManager.getPlayer(e.getPlayer().getName()) == null) {
    		// NOTE: This event is called each time the player changes server
    		// This check ensures this is only handled when the player is first connecting to the proxy
    		final GSPlayer p = PlayerManager.loadPlayer(e.getPlayer());
    		p.setServer(e.getServer().getInfo().getName());

			// Check if an existing player has a "newspawn" flag... send them to new player spawn
    		if ((!p.isFirstJoin()) && (p.isNewSpawn())) {
    	    	SpawnManager.sendPlayerToNewPlayerSpawn(p);
    			p.setNewSpawn(false);
	            DatabaseManager.players.updatePlayer(p);
	            
    		}

    		// Launch the MOTD message scheduler
    		if (ConfigManager.main.MOTD_Enabled && p.firstConnect()) {
    	    	geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
    				@Override
    				public void run() {
    					if (ProxyServer.getInstance().getPlayer(p.getProxiedPlayer().getUniqueId()) != null) {
	    					if (p.isFirstJoin()) {
	    		            	PlayerManager.sendMessageToTarget(e.getPlayer().getName(), ConfigManager.motdNew.getMOTD().replace("{player}", p.getName()));
	    		            } else {
	    		            	PlayerManager.sendMessageToTarget(e.getPlayer().getName(), ConfigManager.motd.getMOTD().replace("{player}", p.getName()));
	    		            }
    	    			}
    				}
    	    	}, 500, TimeUnit.MILLISECONDS); 
	        }

    		p.connected();

    		// Update player tracking information
        	geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
    			@Override
    			public void run() {
    				PlayerManager.updateTracking(p);
    			}
        	}, 100, TimeUnit.MILLISECONDS); 
    	}
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
