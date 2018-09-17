package net.cubespace.geSuitBans.commands;


import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.geSuitBans;
import net.cubespace.geSuitBans.managers.BansManager;


public class BanCommand extends CommandManager<BansManager> {

	public BanCommand(BansManager manager, geSuitBans mod) {
		super(manager, mod);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if (args.length == 1) {
			manager.banPlayer(sender.getName(), args[0], "");
			return true;
		}
		else if (args.length > 0) {
			manager.banPlayer(sender.getName(), args[0], StringUtils.join(args, " ", 1, args.length));
			return true;
		}

		return false;
	}

}
