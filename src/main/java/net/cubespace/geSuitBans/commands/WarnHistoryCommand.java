package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.managers.BansManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarnHistoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String playerName;

		if (args.length == 0) {
            playerName = sender.getName();
        } else {
        	playerName = args[0];
        }
        
		if (sender.hasPermission("gesuit.bans.command.warn")) {
			// Show warnings followed by any active bans
			BansManager.displayPlayerWarnBanHistory(sender.getName(), playerName);
		} else {
			if (!sender.getName().equals(playerName)) {
				sender.sendMessage(ChatColor.RED + "You can only view your own warnings.");
            	return true;
			}

			boolean showStaffNames = false;
			BansManager.displayPlayerWarnHistory(sender.getName(), playerName, showStaffNames);
		}

		return true;

    }
}
