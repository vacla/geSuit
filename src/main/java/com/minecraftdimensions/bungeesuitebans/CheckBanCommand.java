package com.minecraftdimensions.bungeesuitebans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class CheckBanCommand implements CommandExecutor {

	BungeeSuiteBans plugin;
	
	private static final String[] PERMISSION_NODES = { "bungeesuite.ban.checkban", "bungeesuite.ban.*",
		"bungeesuite.mod", "bungeesuite.admin", "bungeesuite.*" };

	public CheckBanCommand(BungeeSuiteBans bungeeSuiteBans){
		plugin = bungeeSuiteBans;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if(args.length>0){
			plugin.utils.checkPlayerBans(sender.getName(),args[0]);
			return true;
		}

		return false;
	}

}
