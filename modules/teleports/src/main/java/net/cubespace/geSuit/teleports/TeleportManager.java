package net.cubespace.geSuit.teleports;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.TeleportMessage;
import net.cubespace.geSuit.core.messages.TeleportRequestMessage;
import net.cubespace.geSuit.core.messages.UpdateBackMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.teleports.misc.LocationUtil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

public class TeleportManager implements ChannelDataReceiver<BaseMessage>, Listener {
    private Channel<BaseMessage> channel;
    private Cache<UUID, Object> pendingTeleports;
    private Set<Player> backIgnoredPlayers;
    
    public TeleportManager() {
        channel = Global.getChannelManager().createChannel("tp", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        
        pendingTeleports = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
        backIgnoredPlayers = Sets.newHashSet();
    }
    
    /**
     * Teleports a player to another player including across servers
     * @param player The player to teleport
     * @param target The player to teleport to
     * @param cause The teleport cause to be used on the bukkit side in cases where teleport is actually done.
     *              Teleports are not done upon changing server usually, instead the login position is changed to the target location.
     * @return True if the teleport was done or requested. False if there was an error.
     */
    public boolean teleport(GlobalPlayer player, GlobalPlayer target, TeleportCause cause) {
        Player localPlayer = Bukkit.getPlayer(player.getUniqueId());
        if (localPlayer == null) {
            return teleportNonLocal(player, target, cause);
        } else {
            Player localTarget = Bukkit.getPlayer(target.getUniqueId());
            
            if (localTarget == null) {
                return teleportNonLocal(player, target, cause);
            } else {
                return localPlayer.teleport(localTarget, cause);
            }
        }
    }
    
    /**
     * Teleports a player to another player including across servers.
     * The player may, depending on permissions, have to stay still for a time before commencing the teleport
     * @param player The player to teleport
     * @param target The player to teleport to
     * @param cause The teleport cause to be used on the bukkit side in cases where teleport is actually done.
     *              Teleports are not done upon changing server usually, instead the login position is changed to the target location.
     */
    public void teleportWithDelay(GlobalPlayer player, GlobalPlayer target, TeleportCause cause) {
        handleTPRequest(new TeleportRequestMessage(player.getUniqueId(), target.getUniqueId(), cause.ordinal()));
    }
    
    private boolean teleportNonLocal(GlobalPlayer player, GlobalPlayer target, TeleportCause cause) {
        TeleportMessage message = new TeleportMessage(player.getUniqueId(), target.getUniqueId(), cause.ordinal(), false);
        channel.send(message, ChannelManager.PROXY);
        
        return true;
    }
    
    /**
     * Teleports a player to a position including across servers
     * @param player The player to teleport
     * @param target The location to teleport to. A null value for server will teleport them on this server. 
     *              A null value for world will teleport them in the same world they are in.
     * @param cause The teleport cause to be used on the bukkit side in cases where teleport is actually done.
     *              Teleports are not done upon changing server usually, instead the login position is changed to the target location.
     * @return True if the teleport was done or requested. False if there was an error.
     */
    public boolean teleport(GlobalPlayer player, Location target, TeleportCause cause) {
        Player localPlayer = Bukkit.getPlayer(player.getUniqueId());
        if (localPlayer == null) {
            return teleportNonLocal(player, target, cause);
        } else {
            if (target.getServer() != null) {
                return teleportNonLocal(player, target, cause);
            }
            
            return teleportLocal(localPlayer, target, cause);
        }
    }
    
    /**
     * Requests that a player be teleported to a position including across servers.
     * The player may, depending on permissions, have to stay still for a time before commencing the teleport
     * @param player The player to teleport
     * @param target The location to teleport to. A null value for server will teleport them on this server. 
     *              A null value for world will teleport them in the same world they are in.
     * @param cause The teleport cause to be used on the bukkit side in cases where teleport is actually done.
     *              Teleports are not done upon changing server usually, instead the login position is changed to the target location.
     */
    public void teleportWithDelay(GlobalPlayer player, Location target, TeleportCause cause) {
        handleTPRequest(new TeleportRequestMessage(player.getUniqueId(), target, cause.ordinal()));
    }
    
    private boolean teleportLocal(Player player, Location target, TeleportCause cause) {
        if (target.getWorld() == null) {
            return player.teleport(new org.bukkit.Location(player.getWorld(), target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch()), cause);
        } else {
            World world = Bukkit.getWorld(target.getWorld());
            if (world != null) {
                return player.teleport(new org.bukkit.Location(world, target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch()), cause);
            } else {
                return false;
            }
        }
    }
    
    private boolean teleportNonLocal(GlobalPlayer player, Location target, TeleportCause cause) {
        TeleportMessage message = new TeleportMessage(player.getUniqueId(), target, cause.ordinal(), false);
        channel.send(message, ChannelManager.PROXY);
        
        return true;
    }
    
    /**
     * Teleports a player to a server. This is just a server change.
     * @param player The player to teleport
     * @param server The server to go to
     */
    public void teleport(GlobalPlayer player, GlobalServer server) {
        channel.send(new TeleportMessage(player.getUniqueId(), server.getName(), TeleportCause.UNKNOWN.ordinal(), false), ChannelManager.PROXY);
    }
    
    /**
     * Teleports a player to a server. This is just a server change.
     * The player may, depending on permissions, have to stay still for a time before commencing the teleport
     * @param player The player to teleport
     * @param server The server to go to
     */
    public void teleportWithDelay(GlobalPlayer player, GlobalServer server) {
        handleTPRequest(new TeleportRequestMessage(player.getUniqueId(), server.getName(), TeleportCause.UNKNOWN.ordinal()));
    }
    
    /**
     * To be called on any join events to check if a login is a result
     * of a teleport of some kind.
     * @param player The player to check
     * @return True if the login is a teleport 
     */
    public boolean isJoinTeleport(Player player) {
        return backIgnoredPlayers.contains(player);
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        System.out.println("Got T " + value);
        if (value instanceof TeleportMessage) {
            handleTeleport((TeleportMessage)value);
        } else if (value instanceof TeleportRequestMessage) {
            handleTPRequest((TeleportRequestMessage)value);
        }
    }
    
    // Handles changing the position players spawn based on pending teleports
    @EventHandler(priority=EventPriority.LOWEST)
    public void onLoginSpawnPosition(final PlayerSpawnLocationEvent event) {
        Object pending = pendingTeleports.getIfPresent(event.getPlayer().getUniqueId());
        if (pending != null) {
            pendingTeleports.invalidate(event.getPlayer().getUniqueId());
            
            if (pending instanceof Location) {
                Location location = (Location)pending;
                
                if (location.getWorld() == null) {
                    event.setSpawnLocation(new org.bukkit.Location(event.getSpawnLocation().getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
                } else {
                    event.setSpawnLocation(new org.bukkit.Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
                }
            } else if (pending instanceof UUID) {
                UUID target = (UUID)pending;
                
                Player targetPlayer = Bukkit.getPlayer(target);
                
                if (targetPlayer != null) {
                    event.setSpawnLocation(targetPlayer.getLocation());
                }
            }
        }
        
        // Prevent recording back location during the first 2 ticks for plugins to move the player around on join
        backIgnoredPlayers.add(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
            @Override
            public void run() {
                backIgnoredPlayers.remove(event.getPlayer());
            }
        }, 2);
    }
    
    private void handleTPRequest(final TeleportRequestMessage message) {
        final Player player = Bukkit.getPlayer(message.player);
        if (player == null) {
            return;
        }
        
        // This will be the teleport message sent to the proxy to do the actual teleport
        final TeleportMessage finalMessage;
        if (message.targetLocation != null) {
            finalMessage = new TeleportMessage(message.player, message.targetLocation, message.cause, true);
        } else if (message.targetPlayer != null) {
            finalMessage = new TeleportMessage(message.player, message.targetPlayer, message.cause, true);
        } else {
            finalMessage = new TeleportMessage(message.player, message.targetServer, message.cause, true);
        }
        
        // Handle the no move policy if needed
        if (!player.hasPermission("gesuit.teleports.bypass.delay")) {
            final org.bukkit.Location initialPosition = player.getLocation();
            
            // TODO: Make time configurable
            player.sendMessage(Global.getMessages().get("teleport.delay.start", "time", "3 seconds"));

            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    
                    // Must be standing in the same block
                    if (initialPosition.getBlock().equals(player.getLocation().getBlock())) {
                        player.sendMessage(Global.getMessages().get("teleport.delay.end"));
                        player.saveData();
                        channel.send(finalMessage, ChannelManager.PROXY);
                    } else {
                        player.sendMessage(Global.getMessages().get("teleport.delay.abort"));
                    }
                }
            }, 60L);
        // Instant teleport
        } else {
            channel.send(finalMessage, ChannelManager.PROXY);
        }
    }
    
    private void handleTeleport(final TeleportMessage message) {
        final TeleportCause cause = TeleportCause.values()[message.cause];
        
        final Player player = Bukkit.getPlayer(message.player);
        org.bukkit.Location targetLocation = null;
        // Teleport to player
        if (message.targetPlayer != null) {
            // Add to pending
            if (player == null) {
                System.out.println("Pending tp " + message.player + " to " + message.targetPlayer);
                pendingTeleports.put(message.player, message.targetPlayer);
            // Process
            } else {
                Player target = Bukkit.getPlayer(message.targetPlayer);
                
                System.out.println("tp " + message.player + " to " + target);
                if (target != null) {
                    targetLocation = target.getLocation();
                }
            }
        // Teleport to location
        } else if (message.targetLocation != null) {
            // Do a world check
            if (message.targetLocation.getWorld() != null) {
                World world = Bukkit.getWorld(message.targetLocation.getWorld());
                if (world == null) {
                    Global.getPlatform().getLogger().warning("Attempted to process teleport for invalid world " + message.targetLocation.getWorld());
                    // TODO: Send something back to inform of this
                    return;
                }
            }
            
            // Add to pending
            if (player == null) {
                System.out.println("pending tp " + message.player + " to " + message.targetLocation);
                pendingTeleports.put(message.player, message.targetLocation);
            // Process
            } else {
                targetLocation = LocationUtil.toBukkit(message.targetLocation, player.getWorld());
                System.out.println("tp " + message.player + " to " + message.targetLocation);
            }
        }
        
        // Teleport must be done on the server thread
        if (targetLocation != null) {
            final org.bukkit.Location fLocation = targetLocation;
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
                @Override
                public void run() {
                    if (message.safe) {
                        doSafeTeleport(player, fLocation, cause);
                    } else {
                        player.teleport(fLocation, cause);
                    }
                }
            });
        }
    }
    
    private void updateBackDeath(final Player player) {
        final org.bukkit.Location loc = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
            @Override
            public void run() {
                channel.send(new UpdateBackMessage(player.getUniqueId(), true, new Location(null, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())), ChannelManager.PROXY);
            }
        });
    }
    
    private void updateBack(final Player player) {
        if (backIgnoredPlayers.contains(player)) {
            return;
        }
        final org.bukkit.Location loc = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
            @Override
            public void run() {
                channel.send(new UpdateBackMessage(player.getUniqueId(), false, new Location(null, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())), ChannelManager.PROXY);
            }
        });
    }
    
    // Handle updating back location
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        updateBackDeath(event.getEntity());
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        updateBack(event.getPlayer());
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerLeaveServer(PlayerQuitEvent event) {
        updateBack(event.getPlayer());
    }
    
    public void doSafeTeleport(Player player, org.bukkit.Location target, TeleportCause cause) {
        // Creative and Spectator are immune from damage so just teleport anyway
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            // Enable flying if needed
            if (LocationUtil.shouldFly(target) && !player.isFlying()) {
                player.setFlying(true);
            }
            player.teleport(target, cause);
            return;
        }
        
        // Try enable fly mode if they're allowed it
        if (LocationUtil.shouldFly(target)) {
            // Already flying
            if (player.isFlying()) {
                player.teleport(target, cause);
                return;
            // Is allowed to enable fly on tp
            } else if (player.hasPermission("gesuit.teleports.tp.fly") || player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.teleport(target, cause);
                return;
            }
        }
        
        // Otherwise do a safe teleport
        player.teleport(LocationUtil.getSafeDestination(target), cause);
    }
}
