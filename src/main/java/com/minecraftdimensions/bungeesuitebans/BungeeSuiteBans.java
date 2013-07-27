package com.minecraftdimensions.bungeesuitebans;



import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BungeeSuiteBans extends JavaPlugin {

	public Utilities utils;

	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteBans";
	boolean tablesCreated = false;
	public BukkitTask createtables;

	public BukkitTask getmessages;

	@Override
	public void onEnable() {
		utils = new Utilities(this);
		registerListeners();
		registerChannels();
		registerCommands();
	}
	
	private void registerCommands() {
		getCommand("kick").setExecutor(new KickCommand(this));
		getCommand("kickall").setExecutor(new KickAllCommand(this));
		getCommand("tempban").setExecutor(new TempBanCommand(this));
		getCommand("ban").setExecutor(new BanCommand(this));
		getCommand("unban").setExecutor(new UnbanCommand(this));
		getCommand("reloadbans").setExecutor(new ReloadBansCommand(this));
		getCommand("ipban").setExecutor(new IPBanCommand(this));
		getCommand("unipban").setExecutor(new UnBanIPCommand(this));
		getCommand("checkban").setExecutor(new CheckBanCommand(this));
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new BansListener(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new BansListener(this), this);
	}


}
