package net.cubespace.geSuitWarps;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuitWarps.commands.DeleteWarpCommand;
import net.cubespace.geSuitWarps.commands.ListWarpsCommand;
import net.cubespace.geSuitWarps.commands.SetWarpCommand;
import net.cubespace.geSuitWarps.commands.SetWarpDescCommand;
import net.cubespace.geSuitWarps.commands.SilentWarpCommand;
import net.cubespace.geSuitWarps.commands.WarpCommand;
import net.cubespace.geSuitWarps.listeners.WarpsListener;

public class geSuitWarps extends BukkitModule {

	public geSuitWarps() {
		super("warps", true);
	}
	
	protected void registerCommands() {
		getCommand("warp").setExecutor(new WarpCommand());
		getCommand("warps").setExecutor(new ListWarpsCommand());
		getCommand("setwarp").setExecutor(new SetWarpCommand());
		getCommand("setwarpdesc").setExecutor(new SetWarpDescCommand());
		getCommand("silentwarp").setExecutor(new SilentWarpCommand());
		getCommand("delwarp").setExecutor(new DeleteWarpCommand());
	}
	
	protected void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new WarpsListener(), this);
	}
}
