package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author benjamincharlton on 29/10/2015.
 */
public class ForceNameHistoryCommand extends Command {
    public ForceNameHistoryCommand() {
        super("!ForceNameUpdate", "gesuit.admin", "!UpdateNameHistory");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

        if (args.length != 1) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_NAMEHISTORYUPDATE_USAGE);
            return;
        }

        PlayerManager.retrieveOldNames(sender, args[0]);
        PlayerManager.sendMessageToTarget(sender, "Player: " + args[0] + " updated.");
        BansManager.displayNameHistory(sender.getName(), args[0]);
    }
}
