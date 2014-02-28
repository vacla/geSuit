package net.cubespace.geSuitTeleports.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitTeleports.managers.TeleportsManager;

public class BackCommand implements CommandExecutor {


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
			TeleportsManager.sendPlayerBack(sender);
			return true;
	}

}
