package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class PortalLiquidListener implements Listener {

	protected BungeeSuitePortals plugin;
	
	private final Location cacheLoc = new Location(null, 0.0, 0.0, 0.0);

	public PortalLiquidListener(BungeeSuitePortals bungeeSuitePortals) {
		plugin = bungeeSuitePortals;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent e) {
		for (Portal p : plugin.portals) {
			if (p.getFillType().isAType(e.getBlock().getTypeId()) && p.isIn(e.getBlock().getLocation(cacheLoc))) {
				e.setCancelled(true);
				break;
			}
		}
	}
	
}
