package net.cubespace.geSuit.teleports.spawns;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.events.GlobalReloadEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.UpdateSpawnMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.storage.StorageSection;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.misc.LocationUtil;

public class SpawnManager implements ChannelDataReceiver<BaseMessage>, Listener {
    // One element for each SpawnType
    private Location spawnGlobal;
    private Location spawnNewPlayer;
    private Location spawnServer;
    private Map<String, Location> spawnWorld;
    
    private Channel<BaseMessage> channel;
    
    public SpawnManager() {
        spawnWorld = Maps.newHashMap();
        
        channel = Global.getChannelManager().createChannel("spawns", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
    }
    
    /**
     * Loads all relevant spawns for this server from the backend
     */
    public void loadSpawns() {
        if (Global.getServer() == null) {
            return;
        }
        
        StorageSection spawns = Global.getStorageProvider().create("gesuit.spawns");
        spawnGlobal = spawns.getSimpleStorable("#global", Location.class);
        spawnNewPlayer = spawns.getSimpleStorable("#new-player", Location.class);
        
        StorageSection serverSpawns = spawns.getSubsection(Global.getServer().getName());
        spawnServer = serverSpawns.getSimpleStorable("spawn", Location.class);
        
        spawnWorld.clear();
        if (serverSpawns.isMap("worlds")) {
            Map<String, String> worldSpawns = serverSpawns.getMap("worlds");
            for (Entry<String, String> spawn : worldSpawns.entrySet()) {
                spawnWorld.put(spawn.getKey(), Location.fromSerialized(spawn.getValue()));
            }
        }
    }
    
    /**
     * Sets the location of a spawn.
     * World spawns use the world of the location
     * @param type The type of spawn
     * @param location The location for the spawn
     */
    public void updateSpawn(SpawnType type, org.bukkit.Location location) {
        if (Global.getServer() == null) {
            return;
        }
        
        Location spawnLoc = LocationUtil.fromBukkit(location, Global.getServer().getName());
        
        StorageSection spawns = Global.getStorageProvider().create("gesuit.spawns");
        switch (type) {
        case Global:
            spawnGlobal = spawnLoc;
            spawns.set("#global", spawnLoc);
            break;
        case NewPlayer:
            spawnNewPlayer = spawnLoc;
            spawns.set("#new-player", spawnLoc);
            break;
        case Server:
            spawnServer = spawnLoc;
            spawns.set(Global.getServer().getName() + ".spawn", spawnLoc);
            break;
        case World: {
            spawnWorld.put(location.getWorld().getName(), spawnLoc);
            Map<String, String> saved = Maps.newHashMapWithExpectedSize(spawnWorld.size());
            for (Entry<String, Location> entry : spawnWorld.entrySet()) {
                saved.put(entry.getKey(), entry.getValue().toSerialized());
            }
            spawns.set(Global.getServer().getName() + ".worlds", saved);
            break;
        }
        }
        
        spawns.update();
        
        channel.broadcast(new UpdateSpawnMessage());
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof UpdateSpawnMessage) {
            loadSpawns();
        }
    }
    
    /**
     * Checks if a spawn type is set
     * @param type The type of spawn to check
     * @param world The world of the spawn used for {@code SpawnType.World}. Can be null for any other type
     * @return True if it is set
     */
    public boolean isSet(SpawnType type, World world) {
        switch (type) {
        case Global:
            return spawnGlobal != null;
        case NewPlayer: 
            return spawnNewPlayer != null;
        case Server:
            return spawnServer != null;
        case World:
            return spawnWorld.containsKey(world.getName());
        default:
            return false;
        }
    }
    
    /**
     * Gets the location of a spawn. 
     * @param type The type of spawn to get the location of
     * @param world The world of the spawn used for {@code SpawnType.World}. Can be null for any other type
     * @return The location of the spawn
     */
    public Location getSpawn(SpawnType type, World world) {
        switch (type) {
        case Global:
            return spawnGlobal;
        case NewPlayer: 
            return spawnNewPlayer;
        case Server:
            return spawnServer;
        case World:
            return spawnWorld.get(world.getName());
        default:
            return null;
        }
    }
    
    /**
     * Unsets a spawn
     * @param type The type of spawn to remove
     * @param world The world of the spawn used for {@code SpawnType.World}. Can be null for any other type
     */
    public void removeSpawn(SpawnType type, World world) {
        if (Global.getServer() == null) {
            return;
        }
        
        StorageSection spawns = Global.getStorageProvider().create("gesuit.spawns");
        switch (type) {
        case Global:
            spawnGlobal = null;
            spawns.remove("#global");
            break;
        case NewPlayer:
            spawnNewPlayer = null;
            spawns.remove("#new-player");
            break;
        case Server:
            spawnServer = null;
            spawns.remove(Global.getServer().getName() + ".spawn");
            break;
        case World: {
            spawnWorld.remove(world.getName());
            Map<String, String> saved = Maps.newHashMapWithExpectedSize(spawnWorld.size());
            for (Entry<String, Location> entry : spawnWorld.entrySet()) {
                saved.put(entry.getKey(), entry.getValue().toSerialized());
            }
            spawns.set(Global.getServer().getName() + ".worlds", saved);
            break;
        }
        }
        
        spawns.update();
        
        channel.broadcast(new UpdateSpawnMessage());
    }
    
    /**
     * Teleports the player to a spawn.
     * This includes a teleport delay for players without a bypass permission 
     * @param type The type of spawn to go to
     * @param player The player to teleport
     * @return True if the spawn exists
     */
    public boolean teleportTo(SpawnType type, Player player) {
        return teleportTo(type, player, true);
    }
    
    /**
     * Teleports the player to a spawn.
     * @param type The type of spawn to go to
     * @param player The player to teleport
     * @param withDelay When true, players without the bypass permission will have 
     * a delay before teleportation. When false, all players will be instant teleported
     * to the spawn.
     * @return True if the spawn exists
     */
    public boolean teleportTo(SpawnType type, Player player, boolean withDelay) {
        Location location;
        switch (type) {
        case Global:
            location = spawnGlobal;
            break;
        case NewPlayer:
            location = spawnNewPlayer;
            break;
        case Server:
            location = spawnServer;
            break;
        case World:
            if (spawnWorld.containsKey(player.getWorld().getName())) {
                location = spawnWorld.get(player.getWorld().getName());
            } else {
                location = spawnServer;
            }
            break;
        default:
            return false;
        }
        
        if (location == null) {
            return false;
        }
        
        if (withDelay) {
            TeleportsModule.getTeleportManager().teleportWithDelay(Global.getPlayer(player.getUniqueId()), location, TeleportCause.COMMAND);
        } else {
            TeleportsModule.getTeleportManager().teleport(Global.getPlayer(player.getUniqueId()), location, TeleportCause.COMMAND);
        }
        return true;
    }
    
    @EventHandler(priority=EventPriority.LOW)
    private void onPlayerSpawnPosition(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        
        if (TeleportsModule.getTeleportManager().isJoinTeleport(player)) {
            return;
        }
        
        if (!player.hasPlayedBefore() && !player.isOp()) {
            if (isSet(SpawnType.World, player.getWorld()) && player.hasPermission("gesuit.spawns.new.world")) {
                event.setSpawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.World, player.getWorld()), null));
            } else if (isSet(SpawnType.Server, null) && player.hasPermission("gesuit.spawns.new.server")) {
                event.setSpawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.Server, null), null));
            } else if (isSet(SpawnType.Global, null) && player.hasPermission("gesuit.spawns.new.global")) {
                Location loc = getSpawn(SpawnType.World, null);
                if (loc.getServer().equals(Global.getServer().getName())) {
                    event.setSpawnLocation(LocationUtil.toBukkit(loc, null));
                } else { 
                    // Why would you... but ok, its your server
                    teleportTo(SpawnType.Global, player, true);
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        if (player.getBedSpawnLocation() != null && player.hasPermission("gesuit.spawns.spawn.bed")) {
            event.setRespawnLocation(player.getBedSpawnLocation());
        } else if (isSet(SpawnType.World, player.getWorld()) && player.hasPermission("gesuit.spawns.spawn.world")) {
            event.setRespawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.World, player.getWorld()), null));
        } else if (isSet(SpawnType.Server, null) && player.hasPermission("gesuit.spawns.spawn.server")) {
            event.setRespawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.World, null), null));
        } else if (isSet(SpawnType.Global, null) && player.hasPermission("gesuit.spawns.spawn.global")) {
            
            // Use local teleport if possible
            Location loc = getSpawn(SpawnType.World, null);
            if (loc.getServer().equals(Global.getServer().getName())) {
                event.setRespawnLocation(LocationUtil.toBukkit(loc, null));
            // We will have to teleport them
            } else {
                if (isSet(SpawnType.World, player.getWorld())) {
                    event.setRespawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.World, player.getWorld()), null));
                } else if (isSet(SpawnType.Server, null)) {
                    event.setRespawnLocation(LocationUtil.toBukkit(getSpawn(SpawnType.World, null), null));
                }
                
                teleportTo(SpawnType.Global, player, false);
            }
        }
    }
    
    @EventHandler
    private void onReload(GlobalReloadEvent event) {
        loadSpawns();
    }
}
