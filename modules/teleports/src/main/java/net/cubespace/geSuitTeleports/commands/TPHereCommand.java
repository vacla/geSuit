package net.cubespace.geSuitTeleports.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;


public class TPHereCommand extends CommandManager<TeleportsManager> {


	public TPHereCommand(TeleportsManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		
		if (args.length == 1) {
			manager.teleportToPlayer(sender, args[0], sender.getName());
			return true;
		}
		return false;
	}

}
