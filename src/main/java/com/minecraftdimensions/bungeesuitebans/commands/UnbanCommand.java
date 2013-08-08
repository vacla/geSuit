package com.minecraftdimensions.bungeesuitebans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.minecraftdimensions.bungeesuitebans.managers.BansManager;


public class UnbanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length==1){
			BansManager.unbanPlayer(sender.getName(), args[0]);
			return true;
		}
			return false;
	}

}
