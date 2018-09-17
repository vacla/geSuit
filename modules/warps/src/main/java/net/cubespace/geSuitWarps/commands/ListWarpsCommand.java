package net.cubespace.geSuitWarps.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitWarps.managers.WarpsManager;


public class ListWarpsCommand extends CommandManager<WarpsManager> {


	public ListWarpsCommand(WarpsManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		manager.listWarps(sender);
		return true;
	}

}
