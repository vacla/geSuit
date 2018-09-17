package net.cubespace.geSuiteSpawn.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;

public class SetWorldSpawnCommand extends CommandManager<SpawnManager> {

	public SetWorldSpawnCommand(SpawnManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		manager.setWorldSpawn(sender);
		return true;

	}

}
