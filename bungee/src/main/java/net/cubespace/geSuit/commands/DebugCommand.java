package net.cubespace.geSuit.commands;

import java.util.UUID;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
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
    			PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug onlineplayers" + ChatColor.WHITE + " - Dump online player list");
    			PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug cachedplayers" + ChatColor.WHITE + " - Dump cached player list");
    		}
    		else if (action.equals("onlineplayers")) {
    			// Useful for troubleshooting issues with the onlinePlayers map
				PlayerManager.sendMessageToTarget(sender, "List of entries in onlinePlayers:");
    			for (String player : PlayerManager.onlinePlayers.keySet()) {
    				GSPlayer gs = PlayerManager.onlinePlayers.get(player);
    				Boolean gsvalid = false;
    				Boolean ppvalid = false;
    				String sname = "";
    				
    				if (gs != null) {
    					gsvalid = true;
    					ProxiedPlayer pp = gs.getProxiedPlayer();
    					if (pp != null) {
    						ppvalid = true;
    						Server s = pp.getServer();
    						if ((s != null) && (s.getInfo() != null)) {
    							sname = s.getInfo().getName();
    						}
    					}
    				}
    				PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + player + 
    						ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +  
    						ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
    						ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
    			}
    		} else if (action.equals("cachedplayers")) {
        			// Useful for troubleshooting issues with the onlinePlayers map
    				PlayerManager.sendMessageToTarget(sender, "List of entries in cachedplayers:");
        			for (UUID uuid : PlayerManager.cachedPlayers.keySet()) {
        				GSPlayer gs = PlayerManager.cachedPlayers.get(uuid);
        				Boolean gsvalid = false;
        				Boolean ppvalid = false;
        				String sname = "";
        				
        				if (gs != null) {
        					gsvalid = true;
        					ProxiedPlayer pp = gs.getProxiedPlayer();
        					if (pp != null) {
        						ppvalid = true;
        						Server s = pp.getServer();
        						if ((s != null) && (s.getInfo() != null)) {
        							sname = s.getInfo().getName();
        						}
        					}
        				}
        				PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + uuid + 
        						ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" + ChatColor.AQUA + " (" + gs.getName() + ")" : ChatColor.RED + "no") +  
        						ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
        						ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
        			}
    		} else {
				PlayerManager.sendMessageToTarget(sender, "ERROR: Invalid debug action");
    		}
    	}
    }
}
