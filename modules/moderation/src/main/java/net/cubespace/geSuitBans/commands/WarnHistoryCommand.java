package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.managers.BansManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarnHistoryCommand implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args)
    {

        if (args.length > 0) {
            BansManager.displayPlayerWarnHistory(sender.getName(), args[0]);
            return true;
        }

        return false;
    }

}
