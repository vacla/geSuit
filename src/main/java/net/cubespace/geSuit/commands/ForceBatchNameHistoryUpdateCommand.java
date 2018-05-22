package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author benjamincharlton on 29/10/2015.
 */
public class ForceBatchNameHistoryUpdateCommand extends Command {
    public ForceBatchNameHistoryUpdateCommand() {
        super("!ForceBatchNameUpdate", "gesuit.admin");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }
        
        if (args.length == 0 || args.length > 2) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages
                    .BUNGEE_COMMAND_BATCHNAMEHISTORYUPDATE_USAGE);
            return;
        }
        boolean all = false;
        String param1 = "";
        String param2 = "";
        if (args[0].equals("all")) all = true;
        
        if (args.length == 2) {
            param1 = args[0];
            param2 = args[1];
        }
        PlayerManager.batchUpdatePlayerNames(sender, all, param1, param2);
    }
}
