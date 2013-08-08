package com.minecraftdimensions.bungeesuitebans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecraftdimensions.bungeesuitebans.commands.BanCommand;
import com.minecraftdimensions.bungeesuitebans.commands.CheckBanCommand;
import com.minecraftdimensions.bungeesuitebans.commands.IPBanCommand;
import com.minecraftdimensions.bungeesuitebans.commands.KickAllCommand;
import com.minecraftdimensions.bungeesuitebans.commands.KickCommand;
import com.minecraftdimensions.bungeesuitebans.commands.ReloadBansCommand;
import com.minecraftdimensions.bungeesuitebans.commands.TempBanCommand;
import com.minecraftdimensions.bungeesuitebans.commands.UnBanIPCommand;
import com.minecraftdimensions.bungeesuitebans.commands.UnbanCommand;
import com.minecraftdimensions.bungeesuitebans.listener.BansListener;


public class BungeeSuiteBans extends JavaPlugin {

	public static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteBans";
	public static BungeeSuiteBans instance;

	@Override
	public void onEnable() {
		instance = this;
		registerChannels();
		registerCommands();
	}
	
	private void registerCommands() {
		getCommand("ban").setExecutor(new BanCommand());
		getCommand("checkban").setExecutor(new CheckBanCommand());
		getCommand("ipban").setExecutor(new IPBanCommand());
		getCommand("kick").setExecutor(new KickCommand());
		getCommand("kickall").setExecutor(new KickAllCommand());
		getCommand("reloadbans").setExecutor(new ReloadBansCommand());
		getCommand("tempban").setExecutor(new TempBanCommand());
		getCommand("unban").setExecutor(new UnbanCommand());
		getCommand("unipban").setExecutor(new UnBanIPCommand());
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new BansListener());
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}


}
