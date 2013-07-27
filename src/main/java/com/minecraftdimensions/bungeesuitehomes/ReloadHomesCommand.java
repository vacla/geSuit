package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadHomesCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public static final String[] PERMISSION_NODES = {
			"bungeesuite.homes.reload", "bungeesuite.homes.*","bungeesuite.admin", "bungeesuite.*" };

	public ReloadHomesCommand(BungeeSuiteHomes bsh) {
		plugin = bsh;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}

			plugin.utils.reloadHomes(sender);
			return true;
	}

}
