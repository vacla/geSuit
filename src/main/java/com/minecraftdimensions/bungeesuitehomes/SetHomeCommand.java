package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetHomeCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public static final String[] PERMISSION_NODES = {
			"bungeesuite.homes.sethome", "bungeesuite.homes.user", "bungeesuite.homes.*", "bungeesuite.*" };

	public SetHomeCommand(BungeeSuiteHomes bst) {
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
			plugin.utils.setPlayersHome(sender, "home");
			return true;
		}else{
			if(args[0].equalsIgnoreCase("home")){
				args[0]="home";
			}
			plugin.utils.setPlayersHome(sender, args[0]);
			return true;
		}
	}

}
