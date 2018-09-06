package net.cubespace.geSuitBans.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitBans.managers.BansManager;


public class KickAllCommand implements CommandExecutor {


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		BansManager.kickAll(sender.getName(), StringUtils.join(args, " "));
		return true;
	}

}
