package net.cubespace.geSuitBans.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitBans.managers.BansManager;


public class KickCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {	

		if (args.length == 1) {
			BansManager.kickPlayer(sender.getName(), args[0], "");
			return true;
		}
		else if (args.length > 1) {
			BansManager.kickPlayer(sender.getName(), args[0], StringUtils.join(args, " ", 1, args.length));
			return true;
		}
		return false;
	}

}
