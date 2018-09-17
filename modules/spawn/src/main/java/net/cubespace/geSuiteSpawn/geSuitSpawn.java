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
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public class geSuitSpawn extends BukkitModule {

	public geSuitSpawn() {
        super("spawns", true);
    }
    
    protected void registerCommands() {
		getCommand("setnewspawn").setExecutor(new SetNewSpawnCommand());
		getCommand("setworldspawn").setExecutor(new SetWorldSpawnCommand());
		getCommand("delworldspawn").setExecutor(new DelWorldSpawnCommand());
		getCommand("setserverspawn").setExecutor(new SetServerSpawnCommand());
		getCommand("setglobalspawn").setExecutor(new SetGlobalSpawnCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("worldspawn").setExecutor(new WorldSpawnCommand());
		getCommand("serverspawn").setExecutor(new ServerSpawnCommand());
		getCommand("globalspawn").setExecutor(new GlobalSpawnCommand());
                getCommand("warpspawn").setExecutor(new WarpSpawnCommand());
	}
	   
    protected void registerListeners() {
    	registerPluginMessageListener(this,new SpawnMessageListener());
    	
		getServer().getPluginManager().registerEvents(new SpawnListener(), this);
	}

}
