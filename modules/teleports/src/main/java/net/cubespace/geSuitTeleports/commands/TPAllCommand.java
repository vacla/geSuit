package net.cubespace.geSuitTeleports.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;


public class TPAllCommand extends CommandManager<TeleportsManager> {

	public TPAllCommand(TeleportsManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length==0){
			manager.tpAll(sender, sender.getName());
			return true;
		}
		if(args.length==1){
			manager.tpAll(sender, args[0]);
			return true;
		}
		return false;
	}

}
