package com.minecraftdimensions.bungeesuiteportals;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortalListener implements Listener {

	BungeeSuitePortals plugin;
	Utilities utils;

	private static String[] PERMISSION_NODES = { "bungeesuite.portal.use.*", "bungeesuite.portal.*",
			"bungeesuite.admin", "bungeesuite.*" };

	public PortalListener(BungeeSuitePortals bungeeSuitePortals) {
		plugin = bungeeSuitePortals;
		utils = plugin.utils;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e) throws IOException {
		Player p = e.getPlayer();
		Location f = e.getFrom();
		Location t = e.getTo();

		if (f.getBlockX() != t.getBlockX() || f.getBlockZ() != t.getBlockZ()
				|| f.getBlockY() != t.getBlockY()) {
			Portal portal = null;
			// NOTE 't.getY() + 0.5' makes sure it fetches the correct block id
			// when walking on half-slabs
			int movingThrough = t.getWorld().getBlockTypeIdAt(t.getBlockX(),
					(int) (t.getY() + 0.5), t.getBlockZ());

			for (Portal portal2 : plugin.portals) {
				if (portal2.isActive()
						&& portal2.getFillType().isAType(movingThrough)
						&& portal2.isIn(t)) {
					portal = portal2;
					break;
				}
			}

			if (portal != null) {

				if (portal.isIn(e.getFrom()))
					return;

				if (CommandUtil.hasPermission(p, PERMISSION_NODES)
						|| p.hasPermission("bungeesuite.portal.use."
								+ portal.getTag())) {

					if (portal.hasWarp())
						utils.warpPlayer(portal.getWarp(), p);
					else
						utils.TeleportPlayerServer(portal.getToServer(), p);

				}else{
					plugin.utils.getMessage(p.getName(), "PORTAL_NO_PERMISSION");
				}

			}
		}
	}

}
