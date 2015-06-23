package net.cubespace.geSuit;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.LangUpdateMessage;
import net.cubespace.geSuit.core.messages.NetworkInfoMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.events.NewPlayerJoinEvent;
import net.cubespace.geSuit.general.GeoIPLookup;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.moderation.BanManager;
import net.cubespace.geSuit.moderation.TrackingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
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
    private geSuitPlugin plugin;
    
    public BungeePlayerManager(Channel<BaseMessage> channel, RedisConnection connection, geSuitPlugin plugin) {
        super(true, channel, connection);
        this.plugin = plugin;
    }
    
    public void initialize(BanManager bans, TrackingManager tracking, GeoIPLookup geoipLookup) {
        this.bans = bans;
        this.tracking = tracking;
        this.geoipLookup = geoipLookup;
        
        broadcastFullUpdate();
        broadcastNetworkInfo();
        broadcastLang();
    }
    
    private void broadcastNetworkInfo() {
        // Build server map
        Map<Integer, String> servers = Maps.newHashMap();
        servers.put(ChannelManager.PROXY, "proxy");
        
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            servers.put(info.getAddress().getPort(), info.getName());
        }
        
        // Send out the update to everyone
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            int id = info.getAddress().getPort();
            channel.send(new NetworkInfoMessage(id, servers), id);
        }
        
        // Update my info
        onNetworkInfo(new NetworkInfoMessage(ChannelManager.PROXY, servers));
    }
    
    private void broadcastLang() {
        LangUpdateMessage message = new LangUpdateMessage(Global.getMessages().getLang(), Global.getMessages().getDefaultLang());
        channel.broadcast(message);
    }
    
    @Override
    protected void onUpdateRequestMessage() {
        super.onUpdateRequestMessage();
        broadcastNetworkInfo();
        broadcastLang();
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
            
            if (player.isNewPlayer()) {
                plugin.getLogger().info(Global.getMessages().get("log.player-create", "player", player.getName(), "uuid", player.getUniqueId()));

                if (ConfigManager.main.NewPlayerBroadcast) {
                    String welcomeMsg = Global.getMessages().get("connect.join.new", "player", player.getDisplayName());
                    ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(welcomeMsg));
                    // Firing custom event
                    ProxyServer.getInstance().getPluginManager().callEvent(new NewPlayerJoinEvent(player.getName(), welcomeMsg));
                } else if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                    net.cubespace.geSuit.managers.PlayerManager.sendBroadcast(Global.getMessages().get("connect.join", "player", player.getDisplayName()));
                }

                // Teleport to new player spawn
                if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && plugin.getSpawnManager().isSetNewPlayer()) {
                    // Somehow we need to make it not connect to this server, only others
                    plugin.getTeleportManager().teleportToInConnection(event.getPlayer(), plugin.getSpawnManager().getSpawnNewPlayer(), event.getServer().getInfo(), true);
                }
            } else {
                if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                    net.cubespace.geSuit.managers.PlayerManager.sendBroadcast(Global.getMessages().get("connect.join", "player", player.getDisplayName()));
                }
            }
            
            // TODO: do all other setup
            
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
        if (ConfigManager.main.BroadcastProxyConnectionMessages) {
            ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    net.cubespace.geSuit.managers.PlayerManager.sendBroadcast(Global.getMessages().get("connect.quit", "player", player.getName()));
                }
            }, ConfigManager.main.PlayerDisconnectDelay, TimeUnit.SECONDS);
        }
        
        // Update time tracking (if enabled)
        if (ConfigManager.bans.TrackOnTime) {
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
