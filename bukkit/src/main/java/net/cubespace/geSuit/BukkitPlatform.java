package net.cubespace.geSuit;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.events.GSEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;

public class BukkitPlatform implements Platform, Listener {
    private Plugin plugin;
    
    public BukkitPlatform(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void callEvent(GSEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }
    
    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
    
    @EventHandler
    private void onNicknameChange(GlobalPlayerNicknameEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
        
        if (player != null) {
            player.setDisplayName(event.getCurrentName());
        }
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    private void onServerLogin(PlayerJoinEvent event) {
        GlobalPlayer player = Global.getPlayer(event.getPlayer().getUniqueId());
        if (player != null && player.hasNickname()) {
            event.getPlayer().setDisplayName(player.getNickname());
        }
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    private void onProxyJoin(GlobalPlayerJoinEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
        
        if (player != null && event.getPlayer().hasNickname()) {
            player.setDisplayName(event.getPlayer().getNickname());
        }
    }
}
