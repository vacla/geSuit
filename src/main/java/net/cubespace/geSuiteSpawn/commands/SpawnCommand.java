package net.cubespace.geSuiteSpawn.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuiteSpawn.geSuitSpawn;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;

public class SpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		final Player player = Bukkit.getPlayer(sender.getName());
        if (!player.hasPermission("gesuit.warps.bypass.delay")) {
            final Location loc = player.getLocation();

            player.sendMessage("&6Teleportation will commence in &c3 seconds&6. Don't move.");
            Bukkit.getServer().getScheduler().runTaskLater(geSuitSpawn.INSTANCE, new Runnable() {
                @Override
                public void run() {
                    if (loc.getBlock().equals(player.getLocation().getBlock())) {
                        player.sendMessage("&6Teleportation commencing...");
            			SpawnManager.sendPlayerToSpawn(player);
                    } else {
                        player.sendMessage("&cTeleportation aborted because you moved.");
                    }
                }
            }, 60L);
            return true;
        } else {
			SpawnManager.sendPlayerToSpawn(player);
            return true;
        }

	}

}
