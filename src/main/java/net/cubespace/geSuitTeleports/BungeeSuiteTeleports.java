package net.cubespace.geSuitTeleports;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.cubespace.geSuitTeleports.commands.BackCommand;
import net.cubespace.geSuitTeleports.commands.TPACommand;
import net.cubespace.geSuitTeleports.commands.TPAHereCommand;
import net.cubespace.geSuitTeleports.commands.TPAcceptCommand;
import net.cubespace.geSuitTeleports.commands.TPAllCommand;
import net.cubespace.geSuitTeleports.commands.TPCommand;
import net.cubespace.geSuitTeleports.commands.TPDenyCommand;
import net.cubespace.geSuitTeleports.commands.TPHereCommand;
import net.cubespace.geSuitTeleports.commands.ToggleCommand;
import net.cubespace.geSuitTeleports.listeners.TeleportsListener;
import net.cubespace.geSuitTeleports.listeners.TeleportsMessageListener;

public class BungeeSuiteTeleports extends JavaPlugin {


	public static String OUTGOING_PLUGIN_CHANNEL = "BSTeleports";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteTP";
	public static BungeeSuiteTeleports instance;

	@Override
	public void onEnable() {
		instance=this;
		registerListeners();
		registerChannels();
		registerCommands();
	}
	
	private void registerCommands() {
		getCommand("tp").setExecutor(new TPCommand());
		getCommand("tphere").setExecutor(new TPHereCommand());
		getCommand("tpall").setExecutor(new TPAllCommand());
		getCommand("tpa").setExecutor(new TPACommand());
		getCommand("tpahere").setExecutor(new TPAHereCommand());
		getCommand("tpaccept").setExecutor(new TPAcceptCommand());
		getCommand("tpdeny").setExecutor(new TPDenyCommand());
		getCommand("back").setExecutor(new BackCommand());
		getCommand("tptoggle").setExecutor(new ToggleCommand());
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new TeleportsMessageListener());
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new TeleportsListener(), this);
	}


}
