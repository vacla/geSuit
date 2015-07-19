package net.cubespace.geSuit.core;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.events.GlobalPlayerJoinMessageEvent;
import net.cubespace.geSuit.events.GlobalPlayerPreLoginEvent;
import net.cubespace.geSuit.events.GlobalPlayerQuitMessageEvent;
import net.cubespace.geSuit.events.JoinNotificationsEvent;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.teleports.SpawnManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerListener implements Listener {
    private final Plugin plugin;
    private final BungeePlayerManager playerManager;
    
    private final SpawnManager spawnManager;
    private final BroadcastManager broadcastManager;
    private final ConfigManager configManager;
    private final Messages messages;
    
    public PlayerListener(Plugin plugin, BungeePlayerManager playerManager, SpawnManager spawnManager, BroadcastManager broadcastManager, ConfigManager configManager, Messages messages) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.spawnManager = spawnManager;
        this.broadcastManager = broadcastManager;
        this.configManager = configManager;
        this.messages = messages;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(final LoginEvent event) {
        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                handlePreLogin(event);
                event.completeIntent(plugin);
            }
        });
    }
    
    private void handlePreLogin(final LoginEvent event) {
        GlobalPlayer player = playerManager.beginPreLogin(event.getConnection());
        
        GlobalPlayerPreLoginEvent loginEvent = new GlobalPlayerPreLoginEvent(player);
        plugin.getProxy().getPluginManager().callEvent(loginEvent);
        
        if (loginEvent.isCancelled()) {
            event.setCancelled(true);
            event.setCancelReason(loginEvent.getCancelMessage());
            return;
        }
        
        plugin.getLogger().info("Player " + player.getDisplayName() + " (" + player.getUniqueId().toString() + ") connected from " + player.getAddress().getHostAddress());
        playerManager.endPreLogin(player);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginComplete(PostLoginEvent event) {
        GlobalPlayer player = playerManager.getPreloadedPlayer(event.getPlayer().getUniqueId());
        if (player != null && player.hasNickname()) {
            event.getPlayer().setDisplayName(player.getNickname());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinComplete(ServerConnectedEvent event) {
        // If a player disconnects immediately after joining, this event can fire after the PlayerDisconnectEvent
        if (plugin.getProxy().getPlayer(event.getPlayer().getUniqueId()) == null) {
            return;
        }
        
        final ProxiedPlayer pPlayer = event.getPlayer();
        
        // We only care about players that are still in the preloaded stage
        if (!playerManager.isPreloaded(pPlayer.getUniqueId())) {
            return;
        }
        
        final GlobalPlayer player = playerManager.finishPreload(event.getPlayer().getUniqueId());
        
        String joinMessage = null;
        // Join broadcast
        if (player.isNewPlayer()) {
            plugin.getLogger().info(messages.get("log.player-create", "player", player.getName(), "uuid", player.getUniqueId()));

            if (configManager.config().NewPlayerBroadcast) {
                joinMessage = messages.get("connect.join.new", "player", player.getDisplayName());
            } else if (configManager.config().BroadcastProxyConnectionMessages) {
                joinMessage = messages.get("connect.join", "player", player.getDisplayName());
            }

            // Teleport to new player spawn
            if (configManager.teleports().SpawnNewPlayerAtNewspawn && spawnManager.isSetNewPlayer()) {
                spawnManager.teleportPlayerToNewSpawn(event.getPlayer(), event.getServer().getInfo());
            }
        } else {
            if (configManager.config().BroadcastProxyConnectionMessages) {
                joinMessage = messages.get("connect.join", "player", player.getDisplayName());
            }
        }
        
        // Provide ability for plugins to change the message
        GlobalPlayerJoinMessageEvent messageEvent = new GlobalPlayerJoinMessageEvent(player, joinMessage);
        plugin.getProxy().getPluginManager().callEvent(messageEvent);
        
        joinMessage = messageEvent.getMessage();
        
        if (!Strings.isNullOrEmpty(joinMessage)) {
            broadcastManager.broadcastGlobal(joinMessage);
        }
        
        // Show MOTD
        if (configManager.config().MOTD_Enabled) {
            String motd = configManager.getMOTD(player.isNewPlayer()).replace("{player}", player.getDisplayName());
            event.getPlayer().sendMessage(motd);
        }
        
        // Notifications
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                JoinNotificationsEvent event = new JoinNotificationsEvent(player);
                plugin.getProxy().getPluginManager().callEvent(event);
                
                for (String message : event.getGlobalMessages()) {
                    broadcastManager.broadcastGlobal(message);
                }
                
                for (Entry<String, String> message : event.getGroupMessages()) {
                    broadcastManager.broadcastGroup(message.getKey(), message.getValue());
                }
                
                for (String message : event.getPrivateMessages()) {
                    pPlayer.sendMessage(TextComponent.fromLegacyText(message));
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerDisconnectEvent event) {
        final GlobalPlayer player = playerManager.getPlayer(event.getPlayer().getUniqueId());
        
        // Send the final disconnect message at this delay
        String quitMessage = null;
        if (configManager.config().BroadcastProxyConnectionMessages) {
            quitMessage = messages.get("connect.quit", "player", player.getDisplayName());
        }
        
        GlobalPlayerQuitMessageEvent quitEvent = new GlobalPlayerQuitMessageEvent(player, quitMessage);
        plugin.getProxy().getPluginManager().callEvent(quitEvent);
        
        if (!Strings.isNullOrEmpty(quitEvent.getMessage())) {
            broadcastManager.broadcastGlobal(quitEvent.getMessage());
        }
        
        // This is the end of their first session. They wont be a new player next time
        if (player.isNewPlayer()) {
            player.setNewPlayer(false);
        }
        
        // Update their last online time
        player.setLastOnline(System.currentTimeMillis());
        
        // Final save before being moved to offline
        player.saveIfModified();
        
        // Handle final remove
        playerManager.removePlayer(player);
    }
}
