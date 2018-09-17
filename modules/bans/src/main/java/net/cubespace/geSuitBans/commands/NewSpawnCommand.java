package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 2/10/2017.
 */
public class NewSpawnCommand extends CommandManager<BansManager> {

    public NewSpawnCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length >= 1) {
            manager.forceToNewSpawn(sender.getName(), args[0]);
            return true;
        }
        return false;
    }
}
