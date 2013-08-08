package com.minecraftdimensions.bungeesuitebans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.minecraftdimensions.bungeesuitebans.managers.BansManager;


public class KickCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {	

		if(args.length==1){
			BansManager.kickPlayer(sender.getName(), args[0], "");
			return true;
		}
		if(args.length>1){
			String msg = "";
			for(String data: args){
				if(!data.equals(args[0])){
					msg+=data+" ";
				}
			}
			BansManager.kickPlayer(sender.getName(), args[0], msg);
			return true;
		}
		return false;
	}

}
