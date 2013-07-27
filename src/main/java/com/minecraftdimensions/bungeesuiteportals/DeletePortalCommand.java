package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class DeletePortalCommand implements CommandExecutor {

	BungeeSuitePortals plugin;

	private static final String[] PERMISSION_NODES = {
			"bungeesuite.portal.delete", "bungeesuite.portal.*",
			"bungeesuite.admin", "bungeesuite.*" };

	public DeletePortalCommand(BungeeSuitePortals bungeeSuitePortals) {
		plugin = bungeeSuitePortals;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if (args.length > 0) {
			plugin.utils.deletePortal(sender.getName(),args[0]);
			return true;
		}
		return false;

	}

}
