package net.cubespace.geSuitPortals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitPortals.managers.PortalsManager;


public class ListPortalsCommand extends CommandManager<PortalsManager> {

    public ListPortalsCommand(PortalsManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        manager.getPortalsList(sender.getName());
        return false;
    }

}
