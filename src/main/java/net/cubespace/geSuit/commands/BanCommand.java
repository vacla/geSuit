package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BanCommand extends Command {
    public BanCommand() {
        super("!ban");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }

        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BUNGEE_COMMAND_BAN_USAGE);
            return;
        }
        
        String reason = null;
        if (args.length > 1) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                if (i != 1) {
                    builder.append(' ');
                }
                
                builder.append(args[i]);
            }
            
            reason = builder.toString();
        }
        
        if (Utilities.isIPAddress(args[0])) {
            BansManager.banIP(sender.getName(), args[0], reason);
        } else {
            if (reason.isEmpty()) {
                // Do not allow a warning without a reason since people accidentally do /db instead of /dw
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BAN_REASON_REQUIRED);
                return;
            }

            BansManager.banPlayer(sender.getName(), args[0], reason);
        }
    }
}
