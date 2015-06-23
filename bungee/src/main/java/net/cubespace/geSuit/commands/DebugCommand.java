package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class DebugCommand extends Command
{

    public DebugCommand()
    {
        super("gsdebug");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
    	if (!(sender.hasPermission("gesuit.debug") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, Global.getMessages().get("player.no-permission"));
            return;
        }

    	if (args.length == 0) {
	
	        // Toggle debug
	        geSuit.getPlugin().setDebugEnabled(!geSuit.getPlugin().isDebugEnabled());
	        
	        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: " + (geSuit.getPlugin().isDebugEnabled() ? "ENABLED" : "DISABLED"));
    	} else {
    		String action = args[0];
    		if (action.equals("help")) {
    			PlayerManager.sendMessageToTarget(sender, ChatColor.GREEN + "geSuit Debug Commands:");
    		} else {
				PlayerManager.sendMessageToTarget(sender, "ERROR: Invalid debug action");
    		}
    	}
    }
}
