package net.cubespace.geSuit.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WhereCommand extends Command {
    public WhereCommand() {
        super("!where");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

//        if (args.length == 0) {
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_WHERE_USAGE);
//            return;
//        }
//
//		String search, options;
//		if (args.length == 1) {
//			options = "";
//			search = args[0];
//		} else {
//			options = args[0];
//			search = args[1];
//		}

		throw new UnsupportedOperationException("Not yet implemented");
        //BansManager.displayWhereHistory(sender.getName(), options, search);
    }
}
