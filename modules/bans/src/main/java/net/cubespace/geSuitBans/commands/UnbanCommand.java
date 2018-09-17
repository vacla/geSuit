package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class UnbanCommand extends CommandManager<BansManager> {

	public UnbanCommand(BansManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length==1){
			manager.unbanPlayer(sender.getName(), args[0]);
			return true;
		}
		sender.sendMessage("Usage: /unban (playername)");
			return false;
	}

}
