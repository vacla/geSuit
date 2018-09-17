package net.cubespace.geSuitHomes.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitHomes.managers.HomesManager;

public class DelHomeCommand extends CommandManager<HomesManager> {

	public DelHomeCommand(HomesManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length>0){
			final String pname;
			final String homename;
			boolean other = false;
			if (args[0].contains(":")) {
				String[] parts = args[0].split(":");
				pname = parts[0];
				homename = (parts.length > 1) ? parts[1] : null;
				other = true;
			} else {
				homename = args[0];
				pname = null;
			}
			if(other){
				if(sender.hasPermission("gesuit.homes.commands.delhomes.other"))
					manager.deleteOtherHime(sender, pname, homename);
			}else{
				manager.deleteHome(sender, homename);
			}
		}else{
			manager.deleteHome(sender, "home");
		}
		return true;
	}

}
