package net.cubespace.geSuitWarps.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitWarps.managers.WarpsManager;

public class DeleteWarpCommand extends CommandManager<WarpsManager> {

    public DeleteWarpCommand(WarpsManager manager) {
        super(manager);
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if(args.length>0){
            manager.deleteWarp(sender, args[0]);
			return true;
		}
		return false;

	}

}
