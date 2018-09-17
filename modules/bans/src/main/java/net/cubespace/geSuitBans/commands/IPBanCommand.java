package net.cubespace.geSuitBans.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class IPBanCommand extends CommandManager<BansManager> {


	public IPBanCommand(BansManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if (args.length == 1) {
			manager.ipBanPlayer(sender.getName(), args[0], "");
			return true;
		}
		else if (args.length > 0) {
			manager.ipBanPlayer(sender.getName(), args[0], StringUtils.join(args, " ", 1, args.length));
			return true;
		}

		return false;
	}

}
