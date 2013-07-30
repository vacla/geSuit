package com.minecraftdimensions.bungeesuitespawn;



import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeSuiteSpawn extends JavaPlugin {

	public Utilities utils;

	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteSpawn";
	
	HashMap<World,Location> spawns = new HashMap<World,Location>();
	
	boolean tablesCreated = false;

	public boolean toWorldSpawn;

	public boolean toServerSpawn;

	public boolean UseSpawnForWorldCommand;
	
	public HashMap<String, Location>locqueue = new HashMap<String,Location>();

	@Override
	public void onEnable() {
		
		utils = new Utilities(this);
		registerListeners();
		registerChannels();
		registerCommands();
	}

	private void registerCommands() {
		getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
		getCommand("setnewspawn").setExecutor(new SetNewSpawnCommand(this));
		getCommand("setworldspawn").setExecutor(new SetWorldSpawnCommand(this));
		getCommand("spawn").setExecutor(new SpawnCommand(this));
		getCommand("worldspawn").setExecutor(new WorldSpawnCommand(this));
		getCommand("globalspawn").setExecutor(new GlobalSpawnCommand(this));
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new SpawnListener(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new SpawnListener(this), this);
	}

}
