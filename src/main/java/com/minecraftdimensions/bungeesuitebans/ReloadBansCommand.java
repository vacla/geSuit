package com.minecraftdimensions.bungeesuitebans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ReloadBansCommand implements CommandExecutor {

	BungeeSuiteBans plugin;

	private static final String[] PERMISSION_NODES = { "bungeesuite.ban.reload", "bungeesuite.ban.*", "bungeesuite.admin", "bungeesuite.*" };

	public ReloadBansCommand(BungeeSuiteBans bungeeSuiteBans){
		plugin = bungeeSuiteBans;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.reloadBans(sender.getName());
		return false;
	}

}
