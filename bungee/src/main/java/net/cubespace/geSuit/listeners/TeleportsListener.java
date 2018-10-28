package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.LeavingServer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TeleportsListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void serverChange(ServerConnectEvent e) {
        if (!e.isCancelled() && e.getPlayer().getServer() != null) {
            GSPlayer player = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) player = PlayerManager.getPlayer(e.getPlayer().getName());
            if (player == null) {
                geSuit.getInstance().getLogger().warning("Player: " + e.getPlayer() + " could not be found by the Gesuit PlayerManager. Could not execute leaving Server.");
                return;
            }
            LeavingServer.execute(player);
        }
    }
}
