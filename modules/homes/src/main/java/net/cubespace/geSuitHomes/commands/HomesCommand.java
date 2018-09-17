package net.cubespace.geSuitHomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitHomes.managers.HomesManager;

public class HomesCommand extends CommandManager<HomesManager> {

    public HomesCommand(HomesManager manager) {
        super(manager);
    }

    @Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
                if (args.length == 1) {
                    if (sender.hasPermission("gesuit.homes.commands.homes.other")) {
                        manager.getOtherHomesList(sender, args[0]);
                    } else {
                    	sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                    }
                } else {
                    manager.getHomesList(sender);
                }
		return true;
	}

}
