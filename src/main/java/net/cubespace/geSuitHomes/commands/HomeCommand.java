package net.cubespace.geSuitHomes.commands;

import net.cubespace.geSuitHomes.geSuitHomes;
import org.bukkit.Bukkit;
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

		if(args.length==0){
            HomesManager.getHomesList(sender);
		} else {
            final Player player = Bukkit.getPlayer(sender.getName());

            if (!player.hasPermission("gesuit.homes.bypass.delay")) {
                Location currentLocation = player.getLocation();
                lastLocation.put(player, player.getLocation());
                player.sendMessage("Teleportation in progress, don't move!");

                geSuitHomes.getInstance().getServer().getScheduler().runTaskLater(geSuitHomes.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (lastLocation.get(player).getBlock().equals(player.getLocation().getBlock())) {
                            player.saveData();
                            HomesManager.sendHome(sender, args[0]);
                        } else {
                            player.sendMessage("You moved, teleportation aborted!");
                        }
                    }
                }, 100L);
            } else {
                player.saveData();
                HomesManager.sendHome(sender, args[0]);
            }
		}
		return true;
	}

}
