package com.minecraftdimensions.bungeesuitewarps;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;
import com.minecraftdimensions.bungeesuitewarps.commands.DeleteWarpCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.ListWarpsCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.SetWarpCommand;
import com.minecraftdimensions.bungeesuitewarps.commands.WarpCommand;
import com.minecraftdimensions.bungeesuitewarps.listeners.WarpsListener;
import com.minecraftdimensions.bungeesuitewarps.listeners.WarpsMessageListener;

public class BungeeSuiteWarps extends JavaPlugin {
	

	public static String OUTGOING_PLUGIN_CHANNEL = "BSWarps";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteWarps";
	public static BungeeSuiteWarps instance;
	public static boolean usingTeleports = false;

	@Override
	public void onEnable() {
		instance=this;
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
		getCommand("warp").setExecutor(new WarpCommand());
		getCommand("warps").setExecutor(new ListWarpsCommand());
		getCommand("setwarp").setExecutor(new SetWarpCommand());
		getCommand("delwarp").setExecutor(new DeleteWarpCommand());
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new WarpsMessageListener());
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new WarpsListener(), this);
	}

}
