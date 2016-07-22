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
        
		if (reason.isEmpty()) {
			// Do not allow a temp ban without a reason since people accidentally do /dtb instead of /dst
			PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.TEMP_BAN_REASON_REQUIRED);
			return;
		}

        // Calculate specified temp ban duration
        int seconds = TimeParser.parseString(args[1]);
        if (seconds == 0) {
            return;
        }

    	BansManager.tempBanPlayer(sender.getName(), args[0], seconds, reason);
    }
}
