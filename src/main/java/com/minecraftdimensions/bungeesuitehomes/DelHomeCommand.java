package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DelHomeCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public static final String[] PERMISSION_NODES = {
			"bungeesuite.homes.delhome", "bungeesuite.homes.*",
			"bungeesuite.homes.user", "bungeesuite.*" };

	public DelHomeCommand(BungeeSuiteHomes bsh) {
		plugin = bsh;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if (args.length == 0) {
			plugin.utils.delHome(sender, "home");
			return true;
		} else {
			if(args[0].equalsIgnoreCase("home")){
				args[0]="home";
			}
			plugin.utils.delHome(sender, args[0]);
			return true;
		}
	}

}
