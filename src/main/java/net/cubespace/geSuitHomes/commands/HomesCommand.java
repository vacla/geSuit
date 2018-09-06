package net.cubespace.geSuitHomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitHomes.managers.HomesManager;

public class HomesCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
                if (args.length == 1) {
                    if (sender.hasPermission("gesuit.homes.commands.homes.other")) {
                    	HomesManager.getOtherHomesList(sender, args[0]);
                    } else {
                    	sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                    }
                } else {
                    HomesManager.getHomesList(sender);
                }
		return true;
	}

}
