package net.cubespace.geSuitPortals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitPortals.managers.PortalsManager;


public class DeletePortalCommand extends CommandManager<PortalsManager> {

    public DeletePortalCommand(PortalsManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length > 0) {
            manager.deletePortal(sender.getName(), args[0]);
            return true;
        }
        return false;

    }

}
