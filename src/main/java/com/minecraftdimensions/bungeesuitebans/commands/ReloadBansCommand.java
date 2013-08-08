package com.minecraftdimensions.bungeesuitebans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.minecraftdimensions.bungeesuitebans.managers.BansManager;


public class ReloadBansCommand implements CommandExecutor {


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		BansManager.reloadBans(sender.getName());
		return false;
	}

}
