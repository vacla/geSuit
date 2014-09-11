package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);
            return;
        }

    	if (args.length == 0) {
	
	        // Toggle debug
	        geSuit.instance.setDebugEnabled(!geSuit.instance.isDebugEnabled());
	        
	        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: " + (geSuit.instance.isDebugEnabled() ? "ENABLED" : "DISABLED"));
    	} else {
    		String action = args[0];
    		if (action.equals("onlineplayers")) {
    			// Useful for troubleshooting issues with the onlinePlayers map
				PlayerManager.sendMessageToTarget(sender, "List of entries in onlinePlayers:");
    			for (String player : PlayerManager.onlinePlayers.keySet()) {
    				GSPlayer gs = PlayerManager.onlinePlayers.get(player);
    				Boolean gsvalid = false;
    				Boolean ppvalid = false;
    				
    				if (gs != null) {
    					gsvalid = true;
    					ProxiedPlayer pp = gs.getProxiedPlayer();
    					if (pp != null) {
    						ppvalid = true; 
    					}
    				}
    				PlayerManager.sendMessageToTarget(sender, "  " + player + " -> GS:" + gsvalid + " / PP:" + ppvalid);
    			}
    		} else {
				PlayerManager.sendMessageToTarget(sender, "ERROR: Invalid debug action");
    		}
    	}
    }
}
