package net.cubespace.geSuitTeleports.commands;

import net.cubespace.geSuitTeleports.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import org.bukkit.entity.Player;


public class TPAcceptCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Must be in game to use this command");
			return false;
		}
		Player p  = Bukkit.getPlayer(sender.getName());
		p.saveData();
		if(LocationUtil.worldGuardTpAllowed(p.getLocation(),p ) || sender.hasPermission("worldgaurd.teleports.allregions"))
			TeleportsManager.tpAccept(sender);
		else sender.sendMessage(ChatColor.RED + "This region will not allow teleporting");
		return true;
	}

}
