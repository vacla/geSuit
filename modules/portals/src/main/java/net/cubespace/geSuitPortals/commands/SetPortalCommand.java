package net.cubespace.geSuitPortals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitPortals.managers.PortalsManager;


public class SetPortalCommand extends CommandManager<PortalsManager> {


	public SetPortalCommand(PortalsManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		//setportal name type dest fill
		if(args.length==3){
			manager.setPortal(sender, args[0], args[1], args[2], "AIR");
			return true;
		}else if (args.length==4){
			manager.setPortal(sender, args[0], args[1], args[2], args[3]);
			return true;
		}
		return false;
	}


}
