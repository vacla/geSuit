package com.minecraftdimensions.bungeesuitespawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetNewSpawnCommand implements CommandExecutor {

	BungeeSuiteSpawn plugin;
	private static final String[] PERMISSION_NODES = {
			"bungeesuite.spawn.setspawn", "bungeesuite.spawn.*",
			"bungeesuite.admin", "bungeesuite.*" };

	public SetNewSpawnCommand(BungeeSuiteSpawn bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.setNewSpawn((Player) sender);
		return true;

	}

}
