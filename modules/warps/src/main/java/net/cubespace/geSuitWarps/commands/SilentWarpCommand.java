package net.cubespace.geSuitWarps.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuitWarps.managers.WarpsManager;

public class SilentWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, Command command,
                             String label, final String[] args) {
        final Player player = Bukkit.getPlayer(sender.getName());
        if (args.length == 1) {
            WarpsManager.silentWarpPlayer(sender, sender.getName(), args[0]);
            return true;

        } else if (args.length > 1 && sender.hasPermission("gesuit.warps.command.warp.other")) {
            WarpsManager.silentWarpPlayer(sender, args[0], args[1]);
            return true;
        }

        // Show the list of warp names (filtered based on user permissions)
        Player p = (Player) sender;
        p.chat("/warps");
        return true;
    }
}
