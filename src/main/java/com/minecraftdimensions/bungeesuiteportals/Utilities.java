package com.minecraftdimensions.bungeesuiteportals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Utilities {
	BungeeSuitePortals plugin;

	public Utilities(BungeeSuitePortals bst) {
		plugin = bst;
	}
	public void getMessage(String sender, String message) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("GetServerMessage");
			out.writeUTF(sender);
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);

	}

	public void createBaseTables() {
		if (!plugin.tablesCreated) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("CreateTable");
				out.writeUTF("BungeePortals");
				out.writeUTF("CREATE TABLE BungeePortals (portalname VARCHAR(50), server VARCHAR(100), toserver VARCHAR(100), towarp VARCHAR(100), world VARCHAR(100), filltype VARCHAR(50), xmax Int, xmin Int, ymax Int, ymin Int, zmax Int, zmin Int)");
			} catch (IOException e) {
				e.printStackTrace();
			}
			new PluginMessageTask(this.plugin,
					Bukkit.getOnlinePlayers()[0], b)
					.runTaskAsynchronously(plugin);
			plugin.tablesCreated = true;
		}
	}

	public void getPortals() {
		if (!plugin.havePortals) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("GetPortals");
			} catch (IOException e) {
				e.printStackTrace();
			}
			plugin.getportals = new PluginMessageTask(this.plugin,
					Bukkit.getOnlinePlayers()[0], b)
					.runTaskLaterAsynchronously(plugin, 20L);
			plugin.havePortals = true;
		}
	}

	public Portal getPortalByPosition(Location l, double offset) {
		for (Portal p : plugin.getPortals()) {
			if (p.isActive() && p.containsLocation(l, offset))
				return p;
		}
		return null;
	}

	public Portal getPortalByPosition(Location l) {
		return this.getPortalByPosition(l, 0);
	}

	public void warpPlayer(String warp, Player player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("PortalWarpPlayer");
			out.writeUTF(player.getName());
			out.writeUTF(player.getName());
			out.writeUTF(warp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, player, b)
				.runTaskAsynchronously(plugin);

	}

	public void TeleportPlayerServer(String toServer, Player p) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF(toServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());

	}

	public void deletePortal(String sender, String portal) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("DeletePortal");
			out.writeUTF(sender);
			out.writeUTF(portal);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void listPortals(String sender) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("ListPortals");
			out.writeUTF(sender);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void createPortal(String sender, String name, String type,
			String dest, String world, String filltype, Region region) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		if (region == null) {
			Bukkit.getPlayer(sender)
					.sendMessage(
							ChatColor.RED
									+ "Please make a selection with the selection tool first!");
			return;
		}
		try {
			out.writeUTF("CreatePortal");
			out.writeUTF(sender);
			out.writeUTF(name);
			out.writeUTF(type);
			out.writeUTF(dest);
			out.writeUTF(world);
			out.writeUTF(filltype);
			out.writeInt(region.getEnd().getBlockX());
			out.writeInt(region.getFirst().getBlockX());
			out.writeInt(region.getEnd().getBlockY());
			out.writeInt(region.getFirst().getBlockY());
			out.writeInt(region.getEnd().getBlockZ());
			out.writeInt(region.getFirst().getBlockZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void getPortal(String name, String type, String dest, String world,
			String filltype, int xmax, int xmin, int ymax, int ymin, int zmax,
			int zmin) {
		World bworld = Bukkit.getWorld(world);
		Location first = new Location(bworld, xmax, ymax, zmax);
		Location end = new Location(bworld, xmin, ymin, zmin);
		FillType ft = FillType.getFillType(filltype);
		Region region = new Region(first, end);
		Portal portal = new Portal(name, type, dest, region, ft);
		plugin.portals.add(portal);

	}

	public void removePortal(String name) {
		for (Portal data : plugin.portals) {
			if(data.getTag().equals(name)){
				data.defillBlocks();
				plugin.portals.remove(data);
				return;
			}
		}
	}

}
