package net.cubespace.geSuitHomes.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitHomes.managers.HomesManager;

public class HomesCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		HomesManager.getHomesList(sender);
		return true;
	}

}
