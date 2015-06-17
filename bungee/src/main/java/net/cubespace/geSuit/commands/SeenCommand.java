package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /seen
 * Permission needed: gesuit.seen or gesuit.admin
 * Arguments: none
 * What does it do: Displays <player> last online time
 */
public class SeenCommand extends Command {
    public SeenCommand() {
        super("seen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.seen") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, Global.getMessages().get("player.no-permisison"));

            return;
        }

        throw new UnsupportedOperationException("Not implemented yet");
//        if (args.length == 0) {
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_SEEN_USAGE);
//            return;
//        }
//
//        PlayerManager.sendMessageToTarget(sender, PlayerManager.getLastSeeninfos(args[0], sender.hasPermission("gesuit.seen.extra"), sender.hasPermission("gesuit.seen.vanish")));
    }
}
