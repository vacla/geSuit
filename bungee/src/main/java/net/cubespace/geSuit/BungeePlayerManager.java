package net.cubespace.geSuit;

import java.net.InetAddress;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.events.NewPlayerJoinEvent;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.moderation.BanManager;
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
    
    public BungeePlayerManager(ChannelManager manager) {
        super(true, manager);
        
        broadcastFullUpdate();
    }
    
    public void initialize(BanManager bans) {
        this.bans = bans;
    }
    
    @EventHandler
    public void onLogin(final LoginEvent event) {
        event.registerIntent(geSuit.getPlugin());
        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
            @Override
            public void run() {
                handleLogin(event);
                event.completeIntent(geSuit.getPlugin());
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
                    geSuit.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being temp banned!");
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelReason(bans.getBanKickReason(player.getBanInfo()));
                geSuit.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being banned!");
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
                    geSuit.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being temp ip-banned!");
                    event.setCancelled(true);
                    return;
                }
            } else {
                event.setCancelReason(bans.getBanKickReason(ipBan));
                geSuit.getLogger().info(ChatColor.RED + player.getName() + "'s connection refused due to being ip-banned!");
                event.setCancelled(true);
                return;
            }
        }
        
        if (!player.hasPlayedBefore()) {
            player.setNewPlayer(true);
        }
        
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
    public void onServerConnect(ServerConnectedEvent event) {
        // Ensure they are still online before continuing
        if (ProxyServer.getInstance().getPlayer(event.getPlayer().getUniqueId()) == null) {
            geSuit.getLogger().warning("ServerConnectedEvent was called on " + event.getPlayer().getName() + " but they're not online");
            return;
        }
        
        if (onServerConnect(event.getPlayer().getUniqueId())) {
            GlobalPlayer player = Global.getPlayer(event.getPlayer().getUniqueId());
            
            // Update the tracking data for this player
            geSuit.getPlugin().getTrackingManager().updateTracking(player);
            
            if (player.isNewPlayer()) {
                LoggingManager.log(ConfigManager.messages.PLAYER_CREATE.replace("{player}", player.getName()).replace("{uuid}", player.getUniqueId().toString()));

                if (ConfigManager.main.NewPlayerBroadcast) {
                    String welcomeMsg = null;
                    net.cubespace.geSuit.managers.PlayerManager.sendBroadcast(welcomeMsg = ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()), player.getName());
                    // Firing custom event
                    ProxyServer.getInstance().getPluginManager().callEvent(new NewPlayerJoinEvent(player.getName(), welcomeMsg));
                }

                if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
//                    SpawnManager.newPlayers.add(player);
//
//                    ProxyServer.getInstance().getScheduler().schedule(geSuit.getPlugin(), new Runnable() {
//
//                        @Override
//                        public void run() {
//                            SpawnManager.sendPlayerToNewPlayerSpawn(gsPlayer);
//                            SpawnManager.newPlayers.remove(player);
//                        }
//
//                    }, 300, TimeUnit.MILLISECONDS);
                }
            }
            // TODO: do all other setup
        }
    }
    
    @EventHandler
    public void onNickname(GlobalPlayerNicknameEvent event) {
        // Update the tracking data for this player
        geSuit.getPlugin().getTrackingManager().updateTracking(event.getPlayer());
    }
    
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        onPlayerLeave(event.getPlayer().getUniqueId());
    }
}
