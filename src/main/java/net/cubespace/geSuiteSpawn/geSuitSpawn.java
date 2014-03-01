package net.cubespace.geSuiteSpawn;

import net.cubespace.geSuiteSpawn.commands.GlobalSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.ServerSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetGlobalSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetNewSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetServerSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetWorldSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SpawnCommand;
import net.cubespace.geSuiteSpawn.commands.WorldSpawnCommand;
import net.cubespace.geSuiteSpawn.listeners.SpawnListener;
import net.cubespace.geSuiteSpawn.listeners.SpawnMessageListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class geSuitSpawn extends JavaPlugin {
	public static Plugin INSTANCE = null;

	@Override
	public void onEnable() {
		INSTANCE = this;
		registerListeners();
		registerChannels();
		registerCommands();
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
				"geSuitSpawns", new SpawnMessageListener());
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				"geSuitSpawns");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new SpawnListener(), this);
	}

}
