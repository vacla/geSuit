package net.cubespace.geSuit;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeePlatform implements Platform, Listener {
    private Plugin plugin;
    
    public BungeePlatform(Plugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }
    
    @Override
    public void callEvent(GSEvent event) {
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }
    
    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
    
    @EventHandler
    public void onNickname(GlobalPlayerNicknameEvent event) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(event.getPlayer().getUniqueId());
        
        if (player != null) {
            if (event.getPlayer().hasNickname()) {
                player.setDisplayName(event.getCurrentName());
            } else {
                player.setDisplayName(player.getName());
            }
        }
    }
}
