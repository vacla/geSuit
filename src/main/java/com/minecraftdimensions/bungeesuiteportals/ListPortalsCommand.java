package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ListPortalsCommand implements CommandExecutor {

	BungeeSuitePortals plugin;
	private static final String[] PERMISSION_NODES = { "bungeesuite.portal.list", "bungeesuite.portal.*",
		"bungeesuite.admin", "bungeesuite.*" };

	public ListPortalsCommand(BungeeSuitePortals bungeeSuitePortals){
		plugin = bungeeSuitePortals;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.listPortals(sender.getName());
		return false;
	}

}
