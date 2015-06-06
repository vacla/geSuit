package net.cubespace.geSuit.portals;

import net.cubespace.geSuit.core.objects.Portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.util.Vector;

public class PortalListener implements Listener {
    private PortalManager manager;
    
    public PortalListener(PortalManager manager) {
        this.manager = manager;
    }
    
    //=========================
    //     Physics updates
    //=========================
    
    // Prevents any updates for portal blocks
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    private void onBlockPhysics(BlockPhysicsEvent event) {
        if (!manager.isPortalMaterial(event.getBlock().getType())) {
            return;
        }
        
        if (!manager.hasPortals(event.getBlock().getWorld())) {
            return;
        }
        
        Portal portal = manager.getPortalAt(event.getBlock());
        if (portal != null) {
            event.setCancelled(true);
        }
    }
    
    // Prevents any updates for portal blocks
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    private void onBlockPhysics(BlockFromToEvent event) {
        if (!manager.isPortalMaterial(event.getBlock().getType())) {
            return;
        }
        
        if (!manager.hasPortals(event.getBlock().getWorld())) {
            return;
        }
        
        Portal portal = manager.getPortalAt(event.getBlock());
        if (portal != null) {
            event.setCancelled(true);
        }
    }
    
    //=========================
    //      Lava portals
    //=========================
    
    private static final int FIRE_SPREAD_RADIUS = 2;
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFireDamage(EntityDamageEvent event) {
        if (!(event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE_TICK)) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (isNearPortal(event.getEntity().getLocation().getBlock(), FIRE_SPREAD_RADIUS)) {
            event.setCancelled(true);
        }
    }
    
    private boolean isNearPortal(Block block, int radius) {
        for (int x = block.getX() - radius; x <= block.getX() + radius; ++x) {
            for (int y = block.getY() - radius; y <= block.getY() + radius; ++y) {
                for (int z = block.getZ() - radius; z <= block.getZ() + radius; ++z) {
                    Block other = block.getWorld().getBlockAt(x, y, z);
                    
                    // Quick check to see if the block is lava
                    if (other.getType() == Material.LAVA || other.getType() == Material.STATIONARY_LAVA) {
                        // See if its a portal
                        if (manager.getPortalAt(other) != null) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    //=========================
    //      using portals
    //=========================
    
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    private void onPlayerEnterPortal(PlayerMoveEvent event) {
        if (!manager.hasPortals(event.getFrom().getWorld())) {
            return;
        }
        
        Block from = event.getFrom().getBlock();
        Block to = event.getTo().getBlock();
        Player player = event.getPlayer();
        
        // We only process when they are moving between blocks to save processing
        if (from.equals(to)) {
            return;
        }
        
        Portal portal = manager.getPortalAt(to);
        if (portal == null) {
            return;
        }
        
        // Check bypass and specific permissions
        if (!player.hasPermission("gesuit.portals.portal.*") && !player.hasPermission("gesuit.portals.portal." + portal.getName())) {
            return;
        }
        
        // Move the player outside the portal
        Vector unitVector = event.getFrom().toVector().subtract(event.getTo().toVector()).normalize();
        Location location = player.getLocation();
        location.setYaw(location.getYaw()+180);
        player.teleport(location);
        player.setVelocity(unitVector.multiply(0.3));
        
        // Use the portal
        manager.onEnterPortal(player, portal);
    }
    
    //=========================
    //   Portal load/unload
    //=========================
    
    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        manager.onWorldLoad(event.getWorld());
    }
    
    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        manager.onWorldUnload(event.getWorld());
    }
}
