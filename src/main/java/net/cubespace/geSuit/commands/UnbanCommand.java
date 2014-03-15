package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnbanCommand extends Command
{

    public UnbanCommand()
    {
        super("!unban");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {

        if (sender instanceof ProxiedPlayer) {
            return;
        }
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_UNBAN_USAGE);
            return;
        }

        BansManager.unbanPlayer(sender.getName(), args[0]);
    }
}
