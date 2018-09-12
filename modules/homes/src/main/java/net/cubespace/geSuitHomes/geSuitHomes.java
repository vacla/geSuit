package net.cubespace.geSuitHomes;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuitHomes.commands.DelHomeCommand;
import net.cubespace.geSuitHomes.commands.HomeCommand;
import net.cubespace.geSuitHomes.commands.HomesCommand;
import net.cubespace.geSuitHomes.commands.ImportHomesCommand;
import net.cubespace.geSuitHomes.commands.SetHomeCommand;
import net.cubespace.geSuitHomes.listeners.HomesListener;
import org.bukkit.Bukkit;

public class geSuitHomes extends BukkitModule {
	
	protected geSuitHomes() {
		super("homes");
	}
	
	protected void registerCommands() {
		getCommand("sethome").setExecutor(new SetHomeCommand());
		getCommand("home").setExecutor(new HomeCommand());
		getCommand("delhome").setExecutor(new DelHomeCommand());
		getCommand("homes").setExecutor(new HomesCommand());
		getCommand("importhomes").setExecutor(new ImportHomesCommand());
	}

	protected  void registerChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, getCHANNEL_NAME());
	}
	
	protected void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new HomesListener(), this);
	}



}
