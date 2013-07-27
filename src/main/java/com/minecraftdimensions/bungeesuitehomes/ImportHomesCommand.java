package com.minecraftdimensions.bungeesuitehomes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ImportHomesCommand implements CommandExecutor {
	BungeeSuiteHomes plugin;

	public ImportHomesCommand(BungeeSuiteHomes bsh) {
		plugin = bsh;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (sender instanceof Player) {
			return false;
		}
		if (sender instanceof ConsoleCommandSender) {
			plugin.utils.importPlayersHomes();
		}
		return true;

	}

}
