package au.com.addstar.geSuitAdmin.commands;

import au.com.addstar.geSuitAdmin.geSuitAdmin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/08/2017.
 */
public class DebugCommand implements CommandExecutor{

    private geSuitAdmin instance;
    public DebugCommand(geSuitAdmin plugin) {
        instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        instance.setDebug(!instance.isDebug());
        commandSender.sendMessage("geSuitAdmin Debugging is " + instance.isDebug() + " for " + instance.getServer().getName());
        return true;
    }
}
