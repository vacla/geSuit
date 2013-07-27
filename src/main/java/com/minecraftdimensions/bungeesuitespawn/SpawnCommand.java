package com.minecraftdimensions.bungeesuitespawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

	BungeeSuiteSpawn plugin;
	private static final String[] PERMISSION_NODES = { "bungeesuite.spawn.spawn", "bungeesuite.spawn.*",
		"bungeesuite.admin", "bungeesuite.*" };
	private static final String[] GLOBAL_NODES = { "bungeesuite.spawn.global", "bungeesuite.spawn.*",
		"bungeesuite.admin", "bungeesuite.*" };


	public SpawnCommand(BungeeSuiteSpawn bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if(CommandUtil.hasPermission(sender, GLOBAL_NODES)){
			plugin.utils.sendPlayerToSpawn(sender.getName());
		}else {
			plugin.utils.worldSpawn((Player)sender);
		}
	
		return true;
	}

}
