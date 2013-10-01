package com.minecraftdimensions.bungeesuiteportals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;


public class ListPortalsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		PortalsManager.getPortalsList(sender.getName());
		return false;
	}

}
