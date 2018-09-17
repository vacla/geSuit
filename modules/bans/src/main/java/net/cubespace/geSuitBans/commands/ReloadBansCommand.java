package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class ReloadBansCommand extends CommandManager<BansManager> {


	public ReloadBansCommand(BansManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		manager.reloadBans(sender.getName());
		return false;
	}

}
