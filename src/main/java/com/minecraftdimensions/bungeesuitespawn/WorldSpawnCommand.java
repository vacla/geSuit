package com.minecraftdimensions.bungeesuitespawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldSpawnCommand implements CommandExecutor {

	BungeeSuiteSpawn plugin;
	private static final String[] PERMISSION_NODES = {
			"bungeesuite.spawn.worldspawn", "bungeesuite.spawn.*", "bungeesuite.admin", "bungeesuite.*" };


	public WorldSpawnCommand(BungeeSuiteSpawn bungeeSuiteSpawn) {
		this.plugin= bungeeSuiteSpawn;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.worldSpawn((Player)sender);
		return true;

	}

}
