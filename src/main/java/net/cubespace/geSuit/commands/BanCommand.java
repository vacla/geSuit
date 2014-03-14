package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /motd Permission needed: gesuit.motd or gesuit.admin Arguments: none What does it do: Prints out the MOTD
 */
public class BanCommand extends Command
{

    public BanCommand()
    {
        super("!ban");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender instanceof CommandSender)) {
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(Utilities.colorize(ConfigManager.messages.BUNGEE_COMMAND_BAN_USAGE));
            return;
        }
        if (!(sender.hasPermission("gesuit.ban") || sender.hasPermission("gesuit.admin"))) {
            sender.sendMessage(Utilities.colorize(ConfigManager.messages.NO_PERMISSION));

            return;
        }
        if (Utilities.isIPAddress(args[0])) {
            BansManager.banIP(sender.getName(), args[0], null);
        }
        else {
            BansManager.banPlayer(sender.getName(), args[0], null);
        }
    }
}
