package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WarnCommand extends Command {
    public WarnCommand() {
        super("!warn");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_WARN_USAGE);
            return;
        }

        // Get reason string from command arguments
        String reason = ""; 
        for (int x=1; x < args.length; x++) {
        	if (reason.isEmpty()) {
        		reason = args[x];
        	} else {
        		reason += " " + args[x];
        	}
        }

        if (reason.isEmpty()) {
			// Do not allow a warning without a reason since people accidentally do /dw instead of /dst
			PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.WARN_REASON_REQUIRED);
			return;
        }

        BansManager.warnPlayer(sender.getName(), args[0], reason);
    }
}
