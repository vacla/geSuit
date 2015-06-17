package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class NamesCommand extends Command {
    public NamesCommand() {
        super("!names", null, "!namehistory");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

//        if (args.length != 1) {
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_NAMES_USAGE);
//            return;
//        }

        throw new UnsupportedOperationException("Not yet implemented");
        //BansManager.displayNameHistory(sender.getName(), args[0]);
    }
}
