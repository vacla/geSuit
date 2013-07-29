package com.minecraftdimensions.bungeesuitebans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TempBanCommand implements CommandExecutor {

	BungeeSuiteBans plugin;
	
	private static final String[] PERMISSION_NODES = { "bungeesuite.ban.tempban", "bungeesuite.ban.*",
		"bungeesuite.mod", "bungeesuite.admin", "bungeesuite.*" };

	public TempBanCommand(BungeeSuiteBans bungeeSuiteBans){
		plugin = bungeeSuiteBans;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if(args.length>1){
			String timing = "";
			for(String data: args){
				if(!data.equals(args[0]));
				timing+=data+" ";
			}
			plugin.utils.tempBanPlayer(sender.getName(), args[0], timing);
			return true;
		}
		return false;
	}

}
