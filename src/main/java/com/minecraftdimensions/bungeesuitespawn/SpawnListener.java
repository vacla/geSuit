package com.minecraftdimensions.bungeesuitespawn;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class SpawnListener implements PluginMessageListener, Listener {

	BungeeSuiteSpawn plugin;

	private static final String[] GLOBAL_RESPAWN_NODES = {
			"bungeesuite.spawn.respawn.global"};
	private static final String[] WORLD_RESPAWN_NODES = {
			"bungeesuite.spawn.respawn.world", "bungeesuite.spawn.*",
			"bungeesuite.admin", "bungeesuite.*" };
	private static final String[] WORLD_SPAWN_NODES = {
			"bungeesuite.spawn.newtoworld", "bungeesuite.spawn.*",
			"bungeesuite.admin", "bungeesuite.*" };

	public SpawnListener(BungeeSuiteSpawn bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerLogin(PlayerJoinEvent e) {
		if (!plugin.tablesCreated) {
			plugin.utils.createBaseTables();
		}
		if (!e.getPlayer().hasPlayedBefore()) {
			if (CommandUtil.hasPermission(e.getPlayer(), WORLD_SPAWN_NODES)) {
				if (plugin.spawns.containsKey(e.getPlayer().getWorld())) {
					e.getPlayer().teleport(
							plugin.spawns.get(e.getPlayer().getWorld()));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerRespawn(PlayerRespawnEvent e) {
		if (CommandUtil.hasPermission(e.getPlayer(), WORLD_RESPAWN_NODES)) {
			if (plugin.spawns.containsKey(e.getPlayer().getWorld())) {
				e.setRespawnLocation(plugin.spawns
						.get(e.getPlayer().getWorld()));
			} else if (plugin.spawns.containsKey(e.getRespawnLocation()
					.getWorld())) {
				e.setRespawnLocation(plugin.spawns.get(e.getRespawnLocation()
						.getWorld()));
			}
		}
		if (CommandUtil.hasPermission(e.getPlayer(), GLOBAL_RESPAWN_NODES)) {
			plugin.utils.sendPlayerToSpawn(e.getPlayer().getName());
		}
	}

	@Override
	public void onPluginMessageReceived(String pluginChannel, Player reciever,
			byte[] message) {
		if (!pluginChannel
				.equalsIgnoreCase(BungeeSuiteSpawn.INCOMING_PLUGIN_CHANNEL))
			return;

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));

		String channel = null;
		try {
			channel = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (channel.equalsIgnoreCase("WorldSpawn")) {

			String loc = null;
			try {
				loc = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String locs[] = loc.split("~");
			plugin.spawns.put(
					Bukkit.getWorld(locs[0]),
					new Location(Bukkit.getWorld(locs[0]), Double
							.parseDouble(locs[1]), Double.parseDouble(locs[2]),
							Double.parseDouble(locs[3]), Float
									.parseFloat(locs[4]), Float
									.parseFloat(locs[5])));
			return;
		}

		String player = null;
		try {
			player = in.readUTF();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (channel.equalsIgnoreCase("SpawnPlayer")) {

			String loc = null;
			try {
				loc = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			plugin.utils.spawnPlayer(player, loc);
		}

	}

}
