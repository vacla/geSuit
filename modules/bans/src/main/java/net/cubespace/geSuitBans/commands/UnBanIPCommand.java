package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.cubespace.geSuitBans.managers.BansManager;


public class UnBanIPCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (args.length > 0) {
            BansManager.unipBanPlayer(sender.getName(), args[0]);
            return true;
        } else {
            sender.sendMessage("Usage: /unipban (ip)");
        }
        return false;
    }

}
