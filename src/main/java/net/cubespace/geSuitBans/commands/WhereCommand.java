package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.cubespace.geSuitBans.managers.BansManager;


public class WhereCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length>0){
			String search, options;
			if (args.length == 1) {
				options = "";
				search = args[0];
			} else {
				options = args[0];
				search = args[1];
			}
			BansManager.displayWhereHistory(sender.getName(), options, search);
			return true;
		}

		return false;
	}

}
