package com.minecraftdimensions.bungeesuitespawn;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Utilities {
	BungeeSuiteSpawn plugin;

	public Utilities(BungeeSuiteSpawn bst) {
		plugin = bst;
	}

	public void createBaseTables() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateTable");
			out.writeUTF("BungeeSpawns");
			out.writeUTF("CREATE TABLE BungeeSpawns (spawnname VARCHAR(50), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE,z DOUBLE,yaw FLOAT, pitch FLOAT, PRIMARY KEY (spawnname), FOREIGN KEY (server) REFERENCES BungeeServers (servername))");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		plugin.tablesCreated=true;
		createSpawnConfig();
		getWorldSpawns();
	}
	



	private void createSpawnConfig() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateSpawnConfig");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	private void getWorldSpawns() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("GetWorldSpawns");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		
	}

	public void sendPlayerToSpawn(String name) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("SendPlayerToSpawn");
			out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void setNewSpawn(Player sender) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Location loc = sender.getLocation();
		try {
			out.writeUTF("SetSpawn");
			out.writeUTF(sender.getName());
			out.writeUTF("newplayerspawn");
			out.writeUTF(loc.getWorld().getName());
			out.writeDouble(loc.getX());
			out.writeDouble(loc.getY());
			out.writeDouble(loc.getZ());
			out.writeFloat(loc.getYaw());
			out.writeFloat(loc.getPitch());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		
	}

	public void setSpawn(Player sender) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Location loc = sender.getLocation();
		sender.sendMessage(loc.getX()+" "+loc.getY()+" "+loc.getZ());
		try {
			out.writeUTF("SetSpawn");
			out.writeUTF(sender.getName());
			out.writeUTF("spawn");
			out.writeUTF(loc.getWorld().getName());
			out.writeDouble(loc.getX());
			out.writeDouble(loc.getY());
			out.writeDouble(loc.getZ());
			out.writeFloat(loc.getYaw());
			out.writeFloat(loc.getPitch());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
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

	public void spawnPlayer(String player, String loc) {
		Player bplayer = Bukkit.getPlayer(player);
		String locs[] = loc.split("~");
		World world = Bukkit.getWorld(locs[1]);
		double x = Double.parseDouble(locs[2]);
		double y = Double.parseDouble(locs[3]);
		double z = Double.parseDouble(locs[4]);
		float yaw = Float.parseFloat(locs[5]);
		float pitch = Float.parseFloat(locs[6]);
		Location location = new Location(world, x, y, z, yaw, pitch);
		if(!location.getChunk().isLoaded()){
			location.getChunk().load();
		}
		if (bplayer != null) {
			bplayer.teleport(location);
		}
		
	}

	public void setWorldSpawn(Player sender) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Location loc = sender.getLocation();
		plugin.spawns.put(loc.getWorld(), loc);
		try {
			out.writeUTF("SetSpawn");
			out.writeUTF(sender.getName());
			out.writeUTF("world");
			out.writeUTF(loc.getWorld().getName());
			out.writeDouble(loc.getX());
			out.writeDouble(loc.getY());
			out.writeDouble(loc.getZ());
			out.writeFloat(loc.getYaw());
			out.writeFloat(loc.getPitch());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		sender.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		sender.sendMessage(ChatColor.DARK_GREEN+"World spawn set");
	}

	public void worldSpawn(Player sender) {
		if(plugin.spawns.containsKey(sender.getWorld())){
			sender.teleport(plugin.spawns.get(sender.getWorld()));
		}else if(plugin.spawns.containsKey(Bukkit.getWorlds().get(0))){
			sender.teleport(plugin.spawns.get(Bukkit.getWorlds().get(0)));
		}else{
			sender.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
		
	}


}
