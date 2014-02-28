package net.cubespace.getSuit.listeners;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.getSuit.managers.ConfigManager;
import net.cubespace.getSuit.managers.PlayerManager;
import net.cubespace.getSuit.managers.SpawnManager;
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
            ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {

                @Override
                public void run() {
                    SpawnManager.sendPlayerToProxySpawn(PlayerManager.getPlayer(e.getPlayer()), false);
                }

            }, 300, TimeUnit.MILLISECONDS);

        }
    }

}
