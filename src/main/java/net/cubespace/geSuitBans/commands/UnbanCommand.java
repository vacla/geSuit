package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.cubespace.geSuitBans.managers.BansManager;


public class UnbanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length==1){
			BansManager.unbanPlayer(sender.getName(), args[0]);
			return true;
		}
		sender.sendMessage("Usage: /unban (playername)");
			return false;
	}

}
