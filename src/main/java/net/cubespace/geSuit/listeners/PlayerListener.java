package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
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
    public void playerLogin(ServerConnectedEvent e) throws SQLException {
    	
    	GSPlayer p = PlayerManager.loadPlayer(e.getPlayer());

        if (ConfigManager.main.MOTD_Enabled) {
            PlayerManager.sendMessageToTarget(e.getPlayer().getName(), ConfigManager.messages.MOTD.replace("{player}", p.getName()));
        }

        p.connected();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogout(final PlayerDisconnectEvent e) {
        int dcTime = ConfigManager.main.PlayerDisconnectDelay;
        final GSPlayer p = PlayerManager.getPlayer(e.getPlayer().getName());
        if (dcTime > 0) {
            geSuit.proxy.getScheduler().schedule(geSuit.instance, new Runnable() {
                @Override
                public void run() {
                    if (ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null) {
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

            }, dcTime, TimeUnit.SECONDS);
        } else {
            if (ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null) {
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
}
