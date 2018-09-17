package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.String;

public class OnTimeCommand extends CommandManager<BansManager> {

    public OnTimeCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args)
    {

        if (args.length > 0) {
            if(args[0].equalsIgnoreCase("top")){
                int page = 1;
                if ((sender instanceof Player) && (!sender.hasPermission("gesuit.bans.command.ontime.top"))) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to do that command.");
                    return true;
                }
                if(args.length == 2){
                    try {
                        page = Integer.parseInt(args[1]);
                    }catch(NumberFormatException e){
                        sender.sendMessage(ChatColor.RED + "You specified an invalid page number.");
                        return true;
                    }
                }
                manager.displayOnTimeTop(sender.getName(), page);
                return true;
            }
        	if ((sender instanceof Player) && (!sender.hasPermission("gesuit.bans.command.ontime.other"))) {
        		sender.sendMessage(ChatColor.RED + "You do not have permission to do that command.");
        		return true;
        	}
            manager.displayOnTimeHistory(sender.getName(), args[0]);
        } else {
            manager.displayOnTimeHistory(sender.getName(), sender.getName());
        }

        return true;
    }

}
