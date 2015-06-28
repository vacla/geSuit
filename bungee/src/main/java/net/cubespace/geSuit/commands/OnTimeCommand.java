package net.cubespace.geSuit.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class OnTimeCommand extends Command {
    public OnTimeCommand() {
        super("!ontime", "", "");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

        throw new UnsupportedOperationException("Not yet implemented");
//        if (args.length == 0) {
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_ONTIME_USAGE);
//            return;
//        } if (args[0].equalsIgnoreCase("top")) {
//            String page = "1";
//            if (args.length == 2) {
//                page = args[1];
//            }
//            BansManager.displayOnTimeTop(sender.getName(), page);
//        } else {
//            BansManager.displayPlayerOnTime(sender.getName(), args[0]);
//        }
    }
}
