package net.cubespace.geSuit;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.events.GlobalPlayerJoinMessageEvent;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.general.GeoIPLookup;
import net.cubespace.geSuit.moderation.BanManager;
import net.cubespace.geSuit.moderation.TrackingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeePlayerManager extends PlayerManager implements Listener {
    private BanManager bans;
    private TrackingManager tracking;
    private GeoIPLookup geoipLookup;
    private BroadcastManager broadcasts;
    private geSuitPlugin plugin;
    
    private ConfigManager configManager;
    
    public BungeePlayerManager(Channel<BaseMessage> channel, ConfigManager configManager, RedisConnection connection, geSuitPlugin plugin) {
        super(true, channel, connection);
        this.configManager = configManager;
        this.plugin = plugin;
    }
    
    public void initialize(BanManager bans, TrackingManager tracking, GeoIPLookup geoipLookup, BroadcastManager broadcasts) {
        this.bans = bans;
        this.tracking = tracking;
        this.geoipLookup = geoipLookup;
        this.broadcasts = broadcasts;
    }
    
    @EventHandler
    public void onLogin(final LoginEvent event) {
        event.registerIntent(plugin);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                handleLogin(event);
                event.completeIntent(plugin);
            }
        });
    }
    
    private void handleLogin(final LoginEvent event) {
        GlobalPlayer player = loadPlayer(event.getConnection().getUniqueId(), event.getConnection().getName(), event.getConnection().getAddress().getAddress());
        
        // Check player ban state
        if (player.isBanned()) {
            if (player.getBanInfo().isTemporary()) {
                // Has ban expired?
                if (System.currentTimeMillis() >= player.getBanInfo().getUntil()) {
                    player.removeBan();
                } else {
                    event.setCancelReason(bans.getBanKickReason(player.getBanInfo()));
                    plugin.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being temp banned!");
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelReason(bans.getBanKickReason(player.getBanInfo()));
                plugin.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being banned!");
                event.setCancelled(true);
                return;
            }
        }
        
        // Check IP ban state
        BanInfo<InetAddress> ipBan = bans.getBan(player.getAddress());
        if (ipBan != null) {
            if (ipBan.isTemporary()) {
                // Has ban expired?
                if (System.currentTimeMillis() >= ipBan.getUntil()) {
                    bans.setBan(player.getAddress(), null);
                } else {
                    event.setCancelReason(bans.getBanKickReason(ipBan));
                    plugin.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being temp ip-banned!");
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelReason(bans.getBanKickReason(ipBan));
                plugin.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being ip-banned!");
                event.setCancelled(true);
                return;
            }
        }
        
        if (!player.hasPlayedBefore()) {
            player.setNewPlayer(true);
        }
        
        plugin.getLogger().info("Player " + player.getDisplayName() + " (" + player.getUniqueId().toString() + ") connected from " + player.getAddress().getHostAddress());
        onPlayerLoginInitComplete(player);
    }
    
    @EventHandler
    public void onLoginComplete(PostLoginEvent event) {
        GlobalPlayer player = getPreloadedPlayer(event.getPlayer().getUniqueId());
        if (player != null && player.hasNickname()) {
            event.getPlayer().setDisplayName(player.getNickname());
        }
    }
    
    @EventHandler
    public void onServerConnect(final ServerConnectedEvent event) {
        // Ensure they are still online before continuing
        if (ProxyServer.getInstance().getPlayer(event.getPlayer().getUniqueId()) == null) {
            plugin.getLogger().warning("ServerConnectedEvent was called on " + event.getPlayer().getName() + " but they're not online");
            return;
        }
        
        if (onServerConnect(event.getPlayer().getUniqueId())) {
            final GlobalPlayer player = Global.getPlayer(event.getPlayer().getUniqueId());
            
            // Update the tracking data for this player
            tracking.updateTracking(player);
            
            String joinMessage = null;
            // Join broadcast
            if (player.isNewPlayer()) {
                plugin.getLogger().info(Global.getMessages().get("log.player-create", "player", player.getName(), "uuid", player.getUniqueId()));

                if (configManager.config().NewPlayerBroadcast) {
                    joinMessage = Global.getMessages().get("connect.join.new", "player", player.getDisplayName());
                } else if (configManager.config().BroadcastProxyConnectionMessages) {
                    joinMessage = Global.getMessages().get("connect.join", "player", player.getDisplayName());
                }

                // Teleport to new player spawn
                if (configManager.teleports().SpawnNewPlayerAtNewspawn && plugin.getSpawnManager().isSetNewPlayer()) {
                    // Somehow we need to make it not connect to this server, only others
                    plugin.getTeleportManager().teleportToInConnection(event.getPlayer(), plugin.getSpawnManager().getSpawnNewPlayer(), event.getServer().getInfo(), true);
                }
            } else {
                if (configManager.config().BroadcastProxyConnectionMessages) {
                    // Name change
                    Track previousName = tracking.checkNameChange(player);
                    if (previousName != null) {
                        joinMessage = Global.getMessages().get(
                                "connect.join.namechange",
                                "player", player.getDisplayName(),
                                "old", previousName.getDisplayName());
                        plugin.getLogger().info(Global.getMessages().get(
                              "connect.join.namechange.log",
                              "player", event.getPlayer().getDisplayName(),
                              "old", previousName.getName()));
                    // Normal join
                    } else {
                        joinMessage = Global.getMessages().get("connect.join", "player", player.getDisplayName());
                    }
                }
            }
            
            // Provide ability for plugins to change the message
            if (joinMessage != null) {
                GlobalPlayerJoinMessageEvent messageEvent = new GlobalPlayerJoinMessageEvent(player, joinMessage);
                Global.getPlatform().callEvent(messageEvent);
                
                joinMessage = messageEvent.getMessage();
                
                if (!Strings.isNullOrEmpty(joinMessage)) {
                    broadcasts.broadcastGlobal(joinMessage);
                }
            }
            
            // Show MOTD
            if (configManager.config().MOTD_Enabled) {
                String motd = configManager.getMOTD(player.isNewPlayer()).replace("{player}", player.getDisplayName());
                event.getPlayer().sendMessage(motd);
            }
            
            // Notifications
            ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    geoipLookup.addPlayerInfo(event.getPlayer(), player);
                    tracking.addPlayerInfo(event.getPlayer(), player);
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }
    
    @EventHandler
    public void onNickname(GlobalPlayerNicknameEvent event) {
        // Update the tracking data for this player
        tracking.updateTracking(event.getPlayer());
    }
    
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        final GlobalPlayer player = getPlayer(event.getPlayer().getUniqueId());
        
        // Send the final disconnect message at this delay
        if (configManager.config().BroadcastProxyConnectionMessages) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    broadcasts.broadcastGlobal(Global.getMessages().get("connect.quit", "player", player.getName()));
                }
            }, configManager.config().PlayerDisconnectDelay, TimeUnit.SECONDS);
        }
        
        // Update time tracking (if enabled)
        if (configManager.moderation().TrackOnTime) {
            tracking.updatePlayerOnTime(player);
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
        onPlayerLeave(event.getPlayer().getUniqueId());
    }
}
