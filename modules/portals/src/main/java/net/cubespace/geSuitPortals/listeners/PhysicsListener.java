package net.cubespace.geSuitPortals.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

import net.cubespace.geSuitPortals.managers.PortalsManager;
import net.cubespace.geSuitPortals.objects.Portal;

import java.util.ArrayList;
import java.util.List;

public class PhysicsListener implements Listener {
    
    private List<Material> toTestFor;
    
    public PhysicsListener() {
        this.toTestFor = new ArrayList<Material>() {
            @Override
            public boolean add(Material o) {
                if (o == null) return false;
                return super.add(o);
            }
        };
        toTestFor.add(Material.getMaterial("NETHER_PORTAL"));
        toTestFor.add(Material.getMaterial("END_PORTAL"));
        toTestFor.add(Material.getMaterial("SUGAR_CANE"));
        toTestFor.add(Material.getMaterial("SUGAR_CANE_BLOCK"));
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (!(blockhasPhysics(e.getBlock()))) {
            return;
        }
        if (!PortalsManager.PORTALS.containsKey(e.getBlock().getWorld())) {
            return;
        }

        for (Portal p : PortalsManager.PORTALS.get(e.getBlock().getWorld())) {
            if (p.isBlockInPortal(e.getBlock())) {
                e.setCancelled(true);
            }
        }

    }
    
    private boolean blockhasPhysics(Block block){
        return (block.isLiquid() || toTestFor.contains(block.getType()));
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPhysics(BlockFromToEvent e) {
        if (!(blockhasPhysics(e.getBlock()))) {
            return;
        }
        if (!PortalsManager.PORTALS.containsKey(e.getBlock().getWorld())) {
            return;
        }

        for (Portal p : PortalsManager.PORTALS.get(e.getBlock().getWorld())) {
            if (p.isBlockInPortal(e.getBlock())) {
                e.setCancelled(true);
            }
        }

    }
}
