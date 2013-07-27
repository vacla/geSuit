package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HomesCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public static final String[] PERMISSION_NODES = {
			"bungeesuite.homes.homes", "bungeesuite.homes.user", "bungeesuite.homes.*","bungeesuite.*" };

	public HomesCommand(BungeeSuiteHomes bst) {
		plugin = bst;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.listPlayersHomes(sender.getName());
		return true;
	}

}
