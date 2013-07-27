package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HomeCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public static final String[] PERMISSION_NODES = {
			"bungeesuite.homes.home", "bungeesuite.homes.user", "bungeesuite.homes.*", "bungeesuite.*" };

	public HomeCommand(BungeeSuiteHomes bst) {
		plugin = bst;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if(args.length==0){
			plugin.utils.sendPlayerHome(sender, "home");
			return true;
		}else{
			plugin.utils.sendPlayerHome(sender, args[0]);
			return true;
		}
	}

}
