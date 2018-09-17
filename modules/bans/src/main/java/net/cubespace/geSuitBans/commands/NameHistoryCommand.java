package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class NameHistoryCommand extends CommandManager<BansManager> {
	public NameHistoryCommand(BansManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length != 1) {
		    return false;
		}

		manager.displayNameHistory(sender.getName(), args[0]);
		
		return true;
	}

}
