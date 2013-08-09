package com.minecraftdimensions.bungeesuitebans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.minecraftdimensions.bungeesuitebans.managers.BansManager;


public class TempBanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length>1){
			String timing = "";
			for(String data: args){
				if(!data.equals(args[0]));
				timing+=data+" ";
			}
			BansManager.tempBanPlayer(sender.getName(), args[0], timing,command);
			return true;
		}
		return false;
	}

}
