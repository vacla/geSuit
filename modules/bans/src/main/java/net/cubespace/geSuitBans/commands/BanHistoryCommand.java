package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BanHistoryCommand extends CommandManager<BansManager> {

    public BanHistoryCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args)
    {

        if (args.length > 0) {
            manager.displayPlayerBanHistory(sender.getName(), args[0]);
            return true;
        }

        return false;
    }

}
