package com.minecraftdimensions.bungeesuitewarps;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class Utilities {
	BungeeSuiteWarps plugin;

	public Utilities(BungeeSuiteWarps bst) {
		plugin = bst;
	}


	public void createBaseTables() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateTable");
			out.writeUTF("BungeeWarps");
			out.writeUTF("CREATE TABLE BungeeWarps (warpname VARCHAR(50), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE,z DOUBLE,yaw FLOAT, pitch FLOAT, private TINYINT(1), PRIMARY KEY (warpname), FOREIGN KEY (server) REFERENCES BungeeServers (servername))");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		plugin.tablesCreated=true;
	}
	
	public void warpRequest(String sender, String player, String warpName, boolean paccess){
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("WarpPlayerB");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeUTF(warpName);
			out.writeBoolean(paccess);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
		.runTaskAsynchronously(plugin);
	}

	public void warpPlayer(String player, String loc) {
		OfflinePlayer bplayer = Bukkit.getOfflinePlayer(player);
		String locs[] = loc.split("~");
		World world = Bukkit.getWorld(locs[2]);
		double x = Double.parseDouble(locs[3]);
		double y = Double.parseDouble(locs[4]);
		double z = Double.parseDouble(locs[5]);
		float yaw = Float.parseFloat(locs[6]);
		float pitch = Float.parseFloat(locs[7]);
		Location location = new Location(world, x, y, z, yaw, pitch);
		if(bplayer.isOnline()){
			bplayer.getPlayer().teleport(location);
		}else{
			plugin.pendingWarps.put(bplayer.getName(),location);
		}
	}
	
	public void createWarp(String sender,String name, String world, double x, double y,
			double z, float yaw, float pitch, boolean hidden){
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateWarp");
			out.writeUTF(sender);
			out.writeUTF(name);
			out.writeUTF(world);
			out.writeDouble(x);
			out.writeDouble(y);
			out.writeDouble(z);
			out.writeFloat(yaw);
			out.writeFloat(pitch);
			out.writeBoolean(hidden);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		
	}
	
	public void deleteWarp(String sender,String name){
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("DeleteWarp");
			out.writeUTF(sender);
			out.writeUTF(name);
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
	public void getWarpList(String player, boolean permission){
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("WarpList");
			out.writeUTF(player);
			out.writeBoolean(permission);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

}
