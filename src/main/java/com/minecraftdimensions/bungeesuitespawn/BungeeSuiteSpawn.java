package com.minecraftdimensions.bungeesuitespawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecraftdimensions.bungeesuitespawn.commands.GlobalSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.SetGlobalSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.SetNewSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.SetServerSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.SetWorldSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.SpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.ServerSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.commands.WorldSpawnCommand;
import com.minecraftdimensions.bungeesuitespawn.listeners.SpawnListener;
import com.minecraftdimensions.bungeesuitespawn.listeners.SpawnMessageListener;
import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;

public class BungeeSuiteSpawn extends JavaPlugin {

	public static Plugin INSTANCE = null;
	public static String OUTGOING_PLUGIN_CHANNEL = "BSSpawns";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteSpawn";
	public static boolean usingTeleports = false;

	@Override
	public void onEnable() {
		INSTANCE = this;
		registerListeners();
		registerChannels();
		registerCommands();
		BungeeSuiteTeleports bt = (BungeeSuiteTeleports) Bukkit.getPluginManager().getPlugin("Teleports");
		if(bt!=null){
			if(bt.getDescription().getAuthors().contains("Bloodsplat")){
				usingTeleports = true;
			}
		}
	}

	private void registerCommands() {
		
		getCommand("setnewspawn").setExecutor(new SetNewSpawnCommand());
		getCommand("setworldspawn").setExecutor(new SetWorldSpawnCommand());
		getCommand("setserverspawn").setExecutor(new SetServerSpawnCommand());
		getCommand("setglobalspawn").setExecutor(new SetGlobalSpawnCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("worldspawn").setExecutor(new WorldSpawnCommand());
		getCommand("serverspawn").setExecutor(new ServerSpawnCommand());
		getCommand("globalspawn").setExecutor(new GlobalSpawnCommand());
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new SpawnMessageListener());
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new SpawnListener(), this);
	}

}
