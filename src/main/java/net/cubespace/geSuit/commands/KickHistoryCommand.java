package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author narimm on 29/10/2015.
 */
public class KickHistoryCommand extends Command {
    public KickHistoryCommand() {
        super("!kickhistory");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }
        if (!ConfigManager.bans.RecordKicks) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_KICKHISTORY_DISABLED);
            return;
        }
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_KICKHISTORY_USAGE);
            return;
        }

        boolean showStaffNames = true;

        BansManager.displayPlayerKickHistory(sender.getName(), args[0], showStaffNames);
    }
}
