package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WarnHistoryCommand extends Command {
    public WarnHistoryCommand() {
        super("!warnhistory", "", "!dst");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

//        if (args.length == 0) {
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_WARNHISTORY_USAGE);
//            return;
//        }

        throw new UnsupportedOperationException("Not yet implemented");
        //BansManager.displayPlayerWarnHistory(sender.getName(), args[0]);
    }
}
