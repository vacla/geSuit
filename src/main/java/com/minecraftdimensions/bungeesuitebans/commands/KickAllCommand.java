package com.minecraftdimensions.bungeesuitebans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.minecraftdimensions.bungeesuitebans.managers.BansManager;


public class KickAllCommand implements CommandExecutor {


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		String msg = "";
		if(args.length>0){
			for(String data: args){
				msg+=data+" ";
			}
		}
		
		BansManager.kickAll(sender.getName(),msg);
		return true;
	}

}
