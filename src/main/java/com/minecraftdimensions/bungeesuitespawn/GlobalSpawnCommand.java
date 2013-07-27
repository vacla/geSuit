package com.minecraftdimensions.bungeesuitespawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GlobalSpawnCommand implements CommandExecutor {

	BungeeSuiteSpawn plugin;
	private static final String[] PERMISSION_NODES = {
			"bungeesuite.spawn.globalspawn", "bungeesuite.spawn.*", "bungeesuite.admin", "bungeesuite.*" };


	public GlobalSpawnCommand(BungeeSuiteSpawn bungeeSuiteSpawn) {
		this.plugin= bungeeSuiteSpawn;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.sendPlayerToSpawn(sender.getName());
		return true;

	}

}
