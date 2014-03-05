package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SpawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void sendPlayerToHub(final PostLoginEvent e) throws SQLException {
        if (ConfigManager.spawn.ForceAllPlayersToProxySpawn && !SpawnManager.newPlayers.contains(e.getPlayer())) {
            ProxyServer.getInstance().getScheduler().schedule(geSuit.instance, new Runnable() {

                @Override
                public void run() {
                    SpawnManager.sendPlayerToProxySpawn(PlayerManager.getPlayer(e.getPlayer()));
                }

            }, 300, TimeUnit.MILLISECONDS);
        }
    }
}
