package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class PortalPhysicsProtectionListner implements Listener {
	
	BungeeSuitePortals plugin;
	
	private final Location cacheLoc = new Location(null, 0.0, 0.0, 0.0);

	public PortalPhysicsProtectionListner(BungeeSuitePortals bungeeSuitePortals) {
		plugin = bungeeSuitePortals;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		for (Portal p : plugin.portals) {
			if (p.isIn(e.getBlock().getLocation(cacheLoc))) {
				e.setCancelled(true);
				break;
			}
		}
	}
	
}
