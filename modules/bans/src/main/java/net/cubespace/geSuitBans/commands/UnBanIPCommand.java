package net.cubespace.geSuitBans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class UnBanIPCommand extends CommandManager<BansManager> {


    public UnBanIPCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (args.length > 0) {
            manager.unipBanPlayer(sender.getName(), args[0]);
            return true;
        } else {
            sender.sendMessage("Usage: /unipban (ip)");
        }
        return false;
    }

}
