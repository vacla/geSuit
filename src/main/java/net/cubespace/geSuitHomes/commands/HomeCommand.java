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

import java.util.HashMap;

public class HomeCommand implements CommandExecutor {

    static HashMap<Player, Location> lastLocation = new HashMap<Player, Location>();

	@Override
	public boolean onCommand(final CommandSender sender, Command command,
			String label, final String[] args) {

		if (args.length==0) {
            HomesManager.getHomesList(sender);
		} else {
            final Player player = Bukkit.getPlayer(sender.getName());
            final String pname;
            final String homename;

            if (args[0].contains(":")) {
            	String[] parts = args[0].split(":");
            	pname = parts[0];
            	homename = (parts.length > 1) ? parts[1] : null;
            } else {
           		homename = args[0];
           		pname = null;
            }

            if ((homename == null) && (pname != null)) {
                // Syntax: "/home player:home"
                if (sender.hasPermission("gesuit.homes.commands.homes.other")) {
                	HomesManager.getOtherHomesList(sender, pname);
                } else {
                	sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                }
            	return true;
            } else {
	            if (!player.hasPermission("gesuit.homes.bypass.delay")) {
	                lastLocation.put(player, player.getLocation());
	                player.sendMessage("Teleportation in progress, don't move!");
	
	                geSuitHomes.getInstance().getServer().getScheduler().runTaskLater(geSuitHomes.getInstance(), new Runnable() {
	                    @Override
	                    public void run() {
	                        if (lastLocation.get(player).getBlock().equals(player.getLocation().getBlock())) {
	                            player.saveData();
	                            if (pname == null) {
	                                HomesManager.sendHome(sender, homename);
	                            } else {
	                            	HomesManager.sendOtherHome(sender, pname, homename);
	                            }
	                        } else {
	                            player.sendMessage("You moved, teleportation aborted!");
	                        }
	                    }
	                }, 100L);
	            } else {
	                player.saveData();
	                if (pname == null) {
	                	HomesManager.sendHome(sender, homename);
	                } else {
	                	HomesManager.sendOtherHome(sender, pname, homename);
	                }
	            }
            }
		}
		return true;
	}
}