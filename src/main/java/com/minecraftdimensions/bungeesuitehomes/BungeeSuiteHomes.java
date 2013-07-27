package com.minecraftdimensions.bungeesuitehomes;


import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeSuiteHomes extends JavaPlugin {

	public Utilities utils;

	public boolean tablesCreated = false;

	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteHomes";
	
	ArrayList<String> groups = new ArrayList<String>();
	HashMap<Player,Location>defaultHomes = new HashMap<Player, Location>();
	

	@Override
	public void onEnable() {
		utils = new Utilities(this);
		registerListeners();
		registerChannels();
		registerCommands();
	}
	
	private void registerCommands() {
		getCommand("sethome").setExecutor(new SetHomeCommand(this));
		getCommand("home").setExecutor(new HomeCommand(this));
		getCommand("delhome").setExecutor(new DelHomeCommand(this));
		getCommand("homes").setExecutor(new HomesCommand(this));
		getCommand("ImportHomes").setExecutor(new ImportHomesCommand(this));
		getCommand("ReloadHomes").setExecutor(new ReloadHomesCommand(this));
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new HomesListener(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new HomesListener(this), this);
	}


}
