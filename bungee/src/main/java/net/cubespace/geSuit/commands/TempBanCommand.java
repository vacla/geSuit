package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.TimeParser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TempBanCommand extends Command {
    public TempBanCommand() {
        super("!tempban");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_TEMPBAN_USAGE);
            return;
        }

        // Get reason string from command arguments
        String reason = ""; 
        for (int x=2; x < args.length; x++) {
        	if (reason.isEmpty()) {
        		reason = args[x];
        	} else {
        		reason += " " + args[x];
        	}
        }
        
        // We never want a completely blank warning
        if (reason.isEmpty()) {
        	reason = "Unspecified";
        }

        // Calculate specified temp ban duration
        int seconds = TimeParser.parseString(args[1]);
        if (seconds == 0) {
            return;
        }

    	BansManager.tempBanPlayer(sender.getName(), args[0], seconds, reason);
    }
}
