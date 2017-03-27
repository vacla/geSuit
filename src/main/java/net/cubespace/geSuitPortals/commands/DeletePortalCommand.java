package net.cubespace.geSuitPortals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitPortals.managers.PortalsManager;


public class DeletePortalCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length > 0) {
            PortalsManager.deletePortal(sender.getName(), args[0]);
            return true;
        }
        return false;

    }

}
