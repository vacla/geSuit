package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class WhereCommand extends CommandManager<BansManager> {

	public WhereCommand(BansManager manager) {
		super(manager);
	}

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
			manager.displayWhereHistory(sender.getName(), options, search);
			return true;
		}

		return false;
	}

}
