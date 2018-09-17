package net.cubespace.geSuitBans.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class WarnCommand extends CommandManager<BansManager> {

	public WarnCommand(BansManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if(args.length==1){
			manager.warnPlayer(sender.getName(), args[0], "");
			return true;
		}
		if(args.length>1){
			StringBuilder msg = new StringBuilder();
			for(String data: args){
				if(!data.equals(args[0])) msg.append(data).append(" ");
			}
			manager.warnPlayer(sender.getName(), args[0], msg.toString());
			return true;
		}

		return false;
	}

}
