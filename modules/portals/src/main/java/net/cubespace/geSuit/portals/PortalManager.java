package net.cubespace.geSuit.portals;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.UpdatePortalMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Portal;
import net.cubespace.geSuit.core.objects.Warp;
import net.cubespace.geSuit.core.objects.Portal.FillType;
import net.cubespace.geSuit.core.storage.StorageSection;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.WarpsModule;

public class PortalManager implements ChannelDataReceiver<BaseMessage> {
    private Map<String, Portal> portals;
    private ListMultimap<World, Portal> worldPortals;
    
    private Channel<BaseMessage> channel;
    
    public PortalManager() {
        portals = Maps.newHashMap();
        worldPortals = ArrayListMultimap.create();
        
        channel = Global.getChannelManager().createChannel("portals", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
    }
    
    /**
     * Loads all portals from the backend. This method is blocking
     */
    public void loadPortals() {
        StorageSection root = Global.getStorage().getSubsection("geSuit.portals");
        
        Set<String> portalNames = root.getSetString("#names");
        portals.clear();
        worldPortals.clear();
        
        if (portalNames == null) {
            return;
        }
        
        for (String name : portalNames) {
            Portal portal = root.getStorable(name, new Portal(name));
            if (portal == null) {
                Global.getPlatform().getLogger().warning("Invalid portal stored in redis under " + name);
                continue;
            }
            
            portals.put(name.toLowerCase(), portal);
            
            if (Global.getServer().getName().equals(portal.getServer())) {
                World world = Bukkit.getWorld(portal.getMinCorner().getWorld());
                worldPortals.put(world, portal);
            }
        }
    }
    
    // Handle loading portals when a world loads
    void onWorldLoad(final World world) {
        List<Portal> matchingPortals = Lists.newArrayList();
        Iterator<Portal> it = worldPortals.get(null).iterator();
        
        while (it.hasNext()) {
            Portal portal = it.next();
            World portalWorld = Bukkit.getWorld(portal.getMinCorner().getWorld());
            
            if (world.equals(portalWorld)) {
                matchingPortals.add(portal);
                it.remove();
            }
        }
        
        worldPortals.putAll(world, matchingPortals);
        
        // Place portals
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
            @Override
            public void run() {
                placePortals(world);
            }
        });
    }
    
    void onWorldUnload(World world) {
        clearPortals(world);
        worldPortals.putAll(null, worldPortals.removeAll(world));
    }
    
    /**
     * Checks if a portal exists
     * @param name The name of the portal, case insensitive
     * @return True if it exists
     */
    public boolean hasPortal(String name) {
        return portals.containsKey(name.toLowerCase());
    }
    
    /**
     * Gets a portal
     * @param name The name of the portal, case insensitive
     * @return The portal, or null
     */
    public Portal getPortal(String name) {
        return portals.get(name.toLowerCase());
    }
    
    /**
     * @return Returns an unmodifiable collections of all portals
     */
    public Collection<Portal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }
    
    /**
     * @return Returns an unmodifiable collections of portals on this server only.
     */
    public Collection<Portal> getLocalPortals() {
        return ImmutableList.copyOf(Iterables.filter(portals.values(), new Predicate<Portal>() {
            @Override
            public boolean apply(Portal input) {
                return input.getServer().equals(Global.getServer().getName());
            }
        }));
    }
    
    /**
     * Creates or updates a portal. <br>
     * <b>NOTE:</b> This method will sync this portal to other servers and is blocking
     * @param portal The portal to set
     * @return True if the portal was created, false if it overrode another portal
     */
    public boolean addOrUpdatePortal(final Portal portal) {
        final Portal existing = portals.put(portal.getName().toLowerCase(), portal);
        // Remove old portal
        boolean remove = false;
        if (existing != null && Global.getServer().getName().equals(existing.getServer())) {
            World world = Bukkit.getWorld(existing.getMinCorner().getWorld());
            worldPortals.remove(world, existing);
            remove = true;
        }
        
        // Add new portal
        boolean place = false;
        if (Global.getServer().getName().equals(portal.getServer())) {
            World world = Bukkit.getWorld(portal.getMinCorner().getWorld());
            worldPortals.put(world, portal);
            place = true;
        }
        
        // Sync it
        StorageSection root = Global.getStorage().getSubsection("geSuit.portals");
        
        root.set("#names", portals.keySet());
        root.set(portal.getName().toLowerCase(), portal);
        root.update();
        
        channel.broadcast(new UpdatePortalMessage());
        
        // Place it if needed
        if (place || remove) {
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
                @Override
                public void run() {
                    if (existing != null) {
                        clearPortal(existing);
                    }
                    placePortal(portal);
                }
            });
        }
        
        return existing == null;
    }
    
    /**
     * Removes a portal. <br>
     * <b>NOTE:</b> This method will sync this portal to other servers and is blocking
     * @param name The name of the portal to remove
     * @return True if the portal was removed
     */
    public boolean removePortal(String name) {
        final Portal portal = portals.remove(name.toLowerCase());
        if (portal == null) {
            return false;
        }
        
        boolean clear = false;
        if (Global.getServer().getName().equals(portal.getServer())) {
            World world = Bukkit.getWorld(portal.getMinCorner().getWorld());
            worldPortals.remove(world, portal);
            clear = true;
        }
        
        // Sync it
        StorageSection root = Global.getStorage().getSubsection("geSuit.portals");
        
        root.set("#names", portals.keySet());
        root.set(portal.getName().toLowerCase(), portal);
        root.update();
        
        channel.broadcast(new UpdatePortalMessage());
        
        // Clear it
        if (clear) {
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(GSPlugin.class), new Runnable() {
                @Override
                public void run() {
                    clearPortal(portal);
                }
            });
        }
        
        return true;
    }

    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof UpdatePortalMessage) {
            loadPortals();
        }
    }
    
    /**
     * Gets the portal that encompasses {@code block}
     * @param block The block in the portal
     * @return The portal at that block, or null
     */
    public Portal getPortalAt(Block block) {
        for (Portal portal : worldPortals.get(block.getWorld())) {
            if (Location.contains(portal.getMinCorner(), portal.getMaxCorner(), block.getX(), block.getY(), block.getZ())) {
                return portal;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if {@code world} has any portals
     * @param world The world to check
     * @return True if it does
     */
    public boolean hasPortals(World world) {
        return worldPortals.containsKey(world);
    }
    
    /**
     * Gets the block Material for the specified FillType
     * @param type The FillType of the portal
     * @return The material that will be used
     */
    public Material getFillMaterial(FillType type) {
        switch (type) {
        case Air:
            return Material.AIR;
        case EndPortal:
            return Material.ENDER_PORTAL;
        case Lava:
            return Material.STATIONARY_LAVA;
        case Portal:
            return Material.PORTAL;
        case SugarCane:
            return Material.SUGAR_CANE_BLOCK;
        case Water:
            return Material.STATIONARY_WATER;
        case Web:
            return Material.WEB;
        default:
            throw new AssertionError();
        }
    }
    
    /**
     * Checks if the material is a possible portal block type
     * @param material The material to check
     * @return True if it can be a portal block
     */
    public boolean isPortalMaterial(Material material) {
        switch (material) {
        case AIR:
        case ENDER_PORTAL:
        case LAVA:
        case STATIONARY_LAVA:
        case WATER:
        case STATIONARY_WATER:
        case PORTAL:
        case SUGAR_CANE_BLOCK:
        case WEB:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * Places all portal blocks for all portals in all worlds on this server.
     * This must be called on the server thread.
     */
    public void placePortals() {
        Preconditions.checkArgument(Bukkit.isPrimaryThread());
        
        for (World world : Bukkit.getWorlds()) {
            placePortals(world);
        }
    }
    
    /**
     * Places all portal blocks for all portals in {@code world}.
     * This must be called on the server thread.
     * @param world The world to place in
     */
    public void placePortals(World world) {
        Preconditions.checkArgument(Bukkit.isPrimaryThread());
        
        for (Portal portal : worldPortals.get(world)) {
            placePortal(portal);
        }
    }
    
    /**
     * Places the blocks for {@code portal}.
     * This must be called on the server thread
     * @param portal The portal to place
     * @return True if it was able to place the blocks
     */
    public boolean placePortal(Portal portal) {
        Preconditions.checkArgument(Bukkit.isPrimaryThread());
        
        Material mat = getFillMaterial(portal.getFill());
        World world = Bukkit.getWorld(portal.getMinCorner().getWorld());
        
        if (world == null) {
            return false;
        }
        
        // Place the block type in each non occupied position in the portals bounds
        for (int x = (int)portal.getMinCorner().getX(); x <= (int)portal.getMaxCorner().getX(); ++x) {
            for (int y = (int)portal.getMinCorner().getY(); y <= (int)portal.getMaxCorner().getY(); ++y) {
                for (int z = (int)portal.getMinCorner().getZ(); z <= (int)portal.getMaxCorner().getZ(); ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.isEmpty()) {
                        block.setType(mat);
                    }
                }
            }
        }
        
        return true;
    }
    
    
    /**
     * Removes all portal blocks for all portals in all worlds on this server.
     * This must be called on the server thread.
     */
    public void clearPortals() {
        for (World world : Bukkit.getWorlds()) {
            clearPortals(world);
        }
    }
    
    /**
     * Removes all portal blocks for all portals in {@code world}.
     * This must be called on the server thread.
     * @param world The world to place in
     */
    public void clearPortals(World world) {
        for (Portal portal : worldPortals.get(world)) {
            clearPortal(portal);
        }
    }
    
    /**
     * Removes the blocks for {@code portal}.
     * This must be called on the server thread
     * @param portal The portal to place
     * @return True if it was able to place the blocks
     */
    public boolean clearPortal(Portal portal) {
        Material mat = getFillMaterial(portal.getFill());
        World world = Bukkit.getWorld(portal.getMinCorner().getWorld());
        
        if (world == null) {
            return false;
        }
        
        // Clear all blocks that match the type
        for (int x = (int)portal.getMinCorner().getX(); x <= (int)portal.getMaxCorner().getX(); ++x) {
            for (int y = (int)portal.getMinCorner().getY(); y <= (int)portal.getMaxCorner().getY(); ++y) {
                for (int z = (int)portal.getMinCorner().getZ(); z <= (int)portal.getMaxCorner().getZ(); ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    if (block.getType() == mat) {
                        block.setType(Material.AIR);
                    // Handles the other states of liquid
                    } else if (mat == Material.STATIONARY_LAVA && block.getType() == Material.LAVA) {
                        block.setType(Material.AIR);
                    } else if (mat == Material.STATIONARY_WATER && block.getType() == Material.WATER) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
        
        return true;
    }
    
    void onEnterPortal(Player player, Portal portal) {
        Location dest;
        switch (portal.getType()) {
        case Server:
            GlobalServer server = Global.getServer(portal.getDestServer());
            if (server != null) {
                TeleportsModule.getTeleportManager().teleport(Global.getPlayer(player.getUniqueId()), server);
            } else {
                player.sendMessage(ChatColor.RED + "Unable to use portal, it is not set up correctly");
                Global.getPlatform().getLogger().warning("Portal " + portal.getName() + " is linked to non-existant server " + portal.getDestServer());
            }
            return;
        case Teleport:
            dest = portal.getDestLocation();
            break;
        case Warp:
            Warp warp = WarpsModule.getWarpManager().getWarp(portal.getDestWarp());
            if (warp != null) {
                dest = warp.getLocation();
            } else {
                player.sendMessage(ChatColor.RED + "Unable to use portal, it is not set up correctly");
                Global.getPlatform().getLogger().warning("Portal " + portal.getName() + " is linked to non-existant warp " + portal.getDestWarp());
                return;
            }
            break;
        default:
            throw new AssertionError();
        }
        
        TeleportsModule.getTeleportManager().teleport(Global.getPlayer(player.getUniqueId()), dest, TeleportCause.PLUGIN);
    }
}
