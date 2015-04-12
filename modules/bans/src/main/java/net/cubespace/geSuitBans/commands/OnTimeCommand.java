package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.managers.BansManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnTimeCommand implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args)
    {

        if (args.length > 0) {
        	if ((sender instanceof Player) && (!sender.hasPermission("gesuit.bans.command.ontime.other"))) {
        		sender.sendMessage(ChatColor.RED + "You do not have permission to do that command.");
        		return true;
        	}
            BansManager.displayOnTimeHistory(sender.getName(), args[0]);
        } else {
            BansManager.displayOnTimeHistory(sender.getName(), sender.getName());
        }

        return true;
    }

}
