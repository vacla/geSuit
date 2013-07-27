package com.minecraftdimensions.bungeesuitewarps;



import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecraftdimensions.bungeesuitewarps.commands.DeleteWarpCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.ListWarpsCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.SetWarpCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.WarpCommand;

public class BungeeSuiteWarps extends JavaPlugin {

	public Utilities utils;

	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteWarps";
	

	boolean tablesCreated = false;

	public HashMap<String,Location> pendingWarps = new HashMap<String,Location>();

	@Override
	public void onEnable() {
		utils = new Utilities(this);
		registerListeners();
		registerChannels();
		registerCommands();
	}

	private void registerCommands() {
		getCommand("bwarp").setExecutor(new WarpCommand(this));
		getCommand("bwarps").setExecutor(new ListWarpsCommand(this));
		getCommand("bsetwarp").setExecutor(new SetWarpCommand(this));
		getCommand("bdelwarp").setExecutor(new DeleteWarpCommand(this));
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new WarpsListener(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new WarpsListener(this), this);
	}

}
