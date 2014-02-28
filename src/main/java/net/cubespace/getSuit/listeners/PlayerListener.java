package net.cubespace.getSuit.listeners;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.getSuit.managers.BackTeleportManager;
import net.cubespace.getSuit.managers.ConfigManager;
import net.cubespace.getSuit.managers.PlayerManager;
import net.cubespace.getSuit.managers.TeleportManager;
import net.cubespace.getSuit.objects.BSPlayer;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogin(PostLoginEvent e) throws SQLException {
        if (!PlayerManager.onlinePlayers.containsKey(e.getPlayer().getName())) {
            PlayerManager.loadPlayer(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogin(ServerConnectedEvent e) throws SQLException {
        BSPlayer p = PlayerManager.getPlayer(e.getPlayer());
        if (p.firstConnect()) {
            if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_CONNECT_PROXY.replace("{player}", p.getName()));
            }

            if (ConfigManager.main.MOTD_Enabled) {
                PlayerManager.sendMessageToPlayer(e.getPlayer().getName(), ConfigManager.messages.MOTD.replace("{player}", p.getName()));
            }

            p.connected();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerSwitchServer(ServerSwitchEvent e) {
        if (BackTeleportManager.contains(e.getPlayer())) {
            TeleportManager.sendTeleportPlayerToLocation(e.getPlayer(), BackTeleportManager.get(e.getPlayer()));
            BackTeleportManager.remove(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerLogout(final PlayerDisconnectEvent e) {
        int dcTime = ConfigManager.main.PlayerDisconnectDelay;
        final BSPlayer p = PlayerManager.getPlayer(e.getPlayer());
        if (dcTime > 0) {
            BungeeSuite.proxy.getScheduler().schedule(BungeeSuite.instance, new Runnable() {

                @Override
                public void run() {
                    if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null) {
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
            if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null) {
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

        BackTeleportManager.remove(e.getPlayer());
    }
}
