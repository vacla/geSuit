package net.cubespace.geSuitWarps.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitWarps.managers.WarpsManager;


public class SetWarpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
				if(args.length==1){
					WarpsManager.setWarp(sender,args[0], false, true);
					return true;
				}else if (args.length==2){
					try{
					WarpsManager.setWarp(sender, args[0], Boolean.parseBoolean(args[1]), true);
					}catch(Exception e){
						sender.sendMessage(ChatColor.RED+"invalid argument supplied");
						return false;
					}
					return true;
				}else if (args.length==3){
					try{
					WarpsManager.setWarp(sender, args[0], Boolean.parseBoolean(args[1]), Boolean.parseBoolean(args[2]));
				}catch(Exception e){
					sender.sendMessage(ChatColor.RED+"invalid argument supplied");
					return false;
				}
					return true;
				}
				return false;

	}

}

