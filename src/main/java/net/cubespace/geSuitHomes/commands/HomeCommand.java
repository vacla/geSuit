package net.cubespace.geSuitHomes.commands;

import net.cubespace.geSuitHomes.geSuitHomes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitHomes.managers.HomesManager;

import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, Command command,
			String label, final String[] args) {

		if (args.length==0) {
            HomesManager.getHomesList(sender);
		} else {
            final Player player = Bukkit.getPlayer(sender.getName());
            final String pname;
            final String homename;

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "This command cannot be run from console.");
                return true;
            }

            if (args[0].contains(":")) {
            	String[] parts = args[0].split(":");
            	pname = parts[0];
            	homename = (parts.length > 1) ? parts[1] : null;
            } else {
           		homename = args[0];
           		pname = null;
            }

            // Check perms if listing player homes or attempting to teleport to one
            if ((pname != null) && (!sender.hasPermission("gesuit.homes.commands.homes.other"))) {
            	sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
            	return true;
            }

            if ((homename == null) && (pname != null)) {
                // Syntax: "/home player:home"
            	HomesManager.getOtherHomesList(sender, pname);
            	return true;
            } else {
	            if (!player.hasPermission("gesuit.homes.bypass.delay")) {
	                final Location lastLocation = player.getLocation();
	                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Teleportation will commence in &c3 seconds&6. Don't move."));
	
	                geSuitHomes.getInstance().getServer().getScheduler().runTaskLater(geSuitHomes.getInstance(), new Runnable() {
	                    @Override
	                    public void run() {
	                    	if (player.isOnline()) {
	                    		if ((lastLocation == null) || (lastLocation.getBlock() == null))
	                    			return;
	                    		
		                        if (lastLocation.getBlock().equals(player.getLocation().getBlock())) {
		                            player.sendMessage(ChatColor.GOLD + "Teleportation commencing...");
		                            player.saveData();
		                            if (pname == null) {
		                                HomesManager.sendHome(sender, homename);
		                            } else {
		                            	HomesManager.sendOtherHome(sender, pname, homename);
		                            }
		                        } else {
		                            player.sendMessage(ChatColor.RED + "Teleportation aborted because you moved.");
		                        }
	                    	}
	                    }
	                }, 60L);
	            } else {
	                player.saveData();
	                if (pname == null) {
	                	// Teleport to own player home
	                	HomesManager.sendHome(sender, homename);
	                } else {
	                	// Teleport player to other player home
                    	HomesManager.sendOtherHome(sender, pname, homename);
	                }
	            }
            }
		}
		return true;
	}
}