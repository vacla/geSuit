package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogin(final ServerConnectedEvent e) throws SQLException {
    	if (PlayerManager.getPlayer(e.getPlayer().getName()) == null) {
    		// NOTE: This event is called each time the player changes server
    		// This check ensures this is only handled when the player is first connecting to the proxy
    		final GSPlayer p = PlayerManager.loadPlayer(e.getPlayer());
    		final boolean newspawn = p.isNewSpawn();
    		p.setServer(e.getServer().getInfo().getName());

			// Check if an existing player has a "newspawn" flag... send them to new player spawn
    		if ((!p.isFirstJoin()) && (newspawn)) {
    	    	SpawnManager.sendPlayerToNewPlayerSpawn(p);
    			p.setNewSpawn(false);
    		}

    		// Check for alt accounts and notify staff
    		String alt = DatabaseManager.players.getAltPlayer(p.getUuid(), p.getIp());
    		if (alt != null) {
    			String msg = ConfigManager.messages.PLAYER_ALT_JOIN.
    					replace("{player}", p.getName()).
    					replace("{alt}", alt).
    					replace("{ip}", p.getIp());
    			Utilities.doBungeeChatMirror("StaffNotice", msg);
    		}

            DatabaseManager.players.updatePlayer(p);
    		
    		// Launch the MOTD message scheduler
    		if (ConfigManager.main.MOTD_Enabled && (p.firstConnect() || newspawn)) {
    	    	geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
    				@Override
    				public void run() {
    					if (ProxyServer.getInstance().getPlayer(p.getProxiedPlayer().getUniqueId()) != null) {
	    					if (p.isFirstJoin() || newspawn) {
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
                    DatabaseManager.players.updatePlayer(p);
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

            geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
                @Override
                public void run() {
                	// Always update the player record when they disconnect
                	DatabaseManager.players.updatePlayer(p);
                }
            }, 1, TimeUnit.MILLISECONDS);
        }
    }
}
