package net.cubespace.geSuiteSpawn.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuiteSpawn.managers.SpawnManager;

public class SetGlobalSpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		SpawnManager.setProxySpawn(sender);
		return true;

	}

}
