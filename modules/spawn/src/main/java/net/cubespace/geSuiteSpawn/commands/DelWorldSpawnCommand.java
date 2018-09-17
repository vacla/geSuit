package net.cubespace.geSuiteSpawn.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;

public class DelWorldSpawnCommand extends CommandManager<SpawnManager> {

	public DelWorldSpawnCommand(SpawnManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		manager.delWorldSpawn(sender);
		return true;

	}

}
