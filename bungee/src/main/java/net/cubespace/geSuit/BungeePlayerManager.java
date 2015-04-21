package net.cubespace.geSuit;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeePlayerManager extends PlayerManager implements Listener {
    public BungeePlayerManager(ChannelManager manager) {
        super(true, manager);
        
        broadcastFullUpdate();
    }
    
    @EventHandler
    public void onLogin(final LoginEvent event) {
        //event.registerIntent(geSuit.getPlugin());
        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
            @Override
            public void run() {
                handleLogin(event);
                //event.completeIntent(geSuit.getPlugin());
            }
        });
    }
    
    private void handleLogin(final LoginEvent event) {
        GlobalPlayer player = loadPlayer(event.getConnection().getUniqueId(), event.getConnection().getName(), event.getConnection().getAddress().getAddress());
        
        // Check ban state
        if (player.isBanned()) {
            if (player.getBanInfo().isTemporary()) {
                // Has ban expired?
                if (System.currentTimeMillis() >= player.getBanInfo().getUntil()) {
                    player.removeBan();
                } else {
                    // TODO: Proper ban message
                    event.setCancelReason("tempbanned");
                    event.setCancelled(true);
                    return;
                }
            } else {
                // TODO: Proper ban message
                event.setCancelReason("banned");
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
            // TODO: do all other setup
        }
    }
    
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        onPlayerLeave(event.getPlayer().getUniqueId());
    }
}
