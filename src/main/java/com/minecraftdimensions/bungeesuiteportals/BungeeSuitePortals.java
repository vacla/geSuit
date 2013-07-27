package com.minecraftdimensions.bungeesuiteportals;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BungeeSuitePortals extends JavaPlugin {

	public Utilities utils;

	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuitePorts";

	boolean tablesCreated = false;
	boolean havePortals = false;
	BukkitTask getportals;
	BukkitTask createtables;
	public RegionSelectionManager rsm;
	boolean portalRegionSelectionMessage = true;

	public ArrayList<Portal> portals = new ArrayList<Portal>();

	@Override
	public void onEnable() {
		utils = new Utilities(this);
		registerListeners();
		registerChannels();
		registerCommands();
	}

	private void registerCommands() {
		getCommand("setportal").setExecutor(new SetPortalCommand(this));
		getCommand("delportal").setExecutor(new DeletePortalCommand(this));
		getCommand("portals").setExecutor(new ListPortalsCommand(this));
	}

	private void registerChannels() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this,
				INCOMING_PLUGIN_CHANNEL, new PortalsListener(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				OUTGOING_PLUGIN_CHANNEL);
		Bukkit.getMessenger()
				.registerOutgoingPluginChannel(this, "BungeeCord");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				rsm = new RegionSelectionManager(this), this);
		getServer().getPluginManager().registerEvents(new PortalListener(this),
				this);
		getServer().getPluginManager().registerEvents(
				new PortalLiquidListener(this), this);
		getServer().getPluginManager().registerEvents(
				new PortalPhysicsProtectionListner(this), this);
		getServer().getPluginManager().registerEvents(
				new PortalsListener(this), this);
	}

	public ArrayList<Portal> getPortals() {
		return portals;
	}
}
