package net.cubespace.geSuitWarps.commands;

import net.cubespace.geSuitWarps.geSuitWarps;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuitWarps.managers.WarpsManager;

import java.util.HashMap;

public class WarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, Command command,
                             String label, final String[] args) {
        final Player player = Bukkit.getPlayer(sender.getName());
        if (args.length == 1) {
            if (!player.hasPermission("gesuit.warps.bypass.delay")) {
                final Location lastLocation = player.getLocation();

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Teleportation will commence in &c3 seconds&6. Don't move."));

                geSuitWarps.getInstance().getServer().getScheduler().runTaskLater(geSuitWarps.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                    	if (player.isOnline()) {
                    		if ((lastLocation == null) || (lastLocation.getBlock() == null))
                    			return;
                    		
    	                    if (lastLocation.getBlock().equals(player.getLocation().getBlock())) {
		                        player.sendMessage(ChatColor.GOLD + "Teleportation commencing...");
		                        WarpsManager.warpPlayer(sender, sender.getName(), args[0]);
		                    } else {
		                        player.sendMessage(ChatColor.RED + "Teleportation aborted because you moved.");
		                    }
                    	}
                    }
                }, 60L);
                return true;
            } else {
                WarpsManager.warpPlayer(sender, sender.getName(), args[0]);
                return true;
            }

        } else if (args.length > 1 && sender.hasPermission("gesuit.warps.command.warp.other")) {
            WarpsManager.warpPlayer(sender, args[0], args[1]);
            return true;
        }

        // Show the list of warp names (filtered based on user permissions)
        Player p = (Player) sender;
        p.chat("/warps");
        return true;
    }
}
