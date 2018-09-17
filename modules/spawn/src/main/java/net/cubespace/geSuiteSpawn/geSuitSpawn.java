package net.cubespace.geSuiteSpawn;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuiteSpawn.commands.GlobalSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.ServerSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetGlobalSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetNewSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetServerSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SetWorldSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.DelWorldSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.SpawnCommand;
import net.cubespace.geSuiteSpawn.commands.WarpSpawnCommand;
import net.cubespace.geSuiteSpawn.commands.WorldSpawnCommand;
import net.cubespace.geSuiteSpawn.listeners.SpawnListener;
import net.cubespace.geSuiteSpawn.listeners.SpawnMessageListener;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;

public class geSuitSpawn extends BukkitModule {

	private SpawnManager manager;
	public geSuitSpawn() {
        super("spawns", true);
		manager = new SpawnManager(this);
    }

	public SpawnManager getManager() {
		return manager;
	}
    protected void registerCommands() {
		getCommand("setnewspawn").setExecutor(new SetNewSpawnCommand(manager));
		getCommand("setworldspawn").setExecutor(new SetWorldSpawnCommand(manager));
		getCommand("delworldspawn").setExecutor(new DelWorldSpawnCommand(manager));
		getCommand("setserverspawn").setExecutor(new SetServerSpawnCommand(manager));
		getCommand("setglobalspawn").setExecutor(new SetGlobalSpawnCommand(manager));
		getCommand("spawn").setExecutor(new SpawnCommand(manager, this));
		getCommand("worldspawn").setExecutor(new WorldSpawnCommand(manager, this));
		getCommand("serverspawn").setExecutor(new ServerSpawnCommand(manager, this));
		getCommand("globalspawn").setExecutor(new GlobalSpawnCommand(manager, this));
		getCommand("warpspawn").setExecutor(new WarpSpawnCommand(manager, this));
	}
	   
    protected void registerListeners() {
		registerPluginMessageListener(this, new SpawnMessageListener(this, manager));
		getServer().getPluginManager().registerEvents(new SpawnListener(manager, this), this);
	}

}
