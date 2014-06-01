package net.cubespace.geSuitWarps.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuitWarps.managers.WarpsManager;

public class WarpCommand implements CommandExecutor {


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length==1){
            WarpsManager.warpPlayer(sender, sender.getName(), args[0]);
			return true;
		}else if(args.length>1 && sender.hasPermission("gesuit.warps.command.warp.other")){
			WarpsManager.warpPlayer(sender, args[0], args[1]);
			return true;
		}
		Player p = (Player) sender;
		p.chat("/warps");
		return true;
	}

}
