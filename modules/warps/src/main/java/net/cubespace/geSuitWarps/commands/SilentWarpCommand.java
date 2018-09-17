package net.cubespace.geSuitWarps.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitWarps.managers.WarpsManager;

public class SilentWarpCommand extends CommandManager<WarpsManager> {

    public SilentWarpCommand(WarpsManager manager, BukkitModule mod) {
        super(manager, mod);
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command,
                             String label, final String[] args) {
        final Player player = Bukkit.getPlayer(sender.getName());
        if (args.length == 1) {
            manager.silentWarpPlayer(sender, sender.getName(), args[0]);
            return true;

        } else if (args.length > 1 && sender.hasPermission("gesuit.warps.command.warp.other")) {
            manager.silentWarpPlayer(sender, args[0], args[1]);
            return true;
        }

        // Show the list of warp names (filtered based on user permissions)
        Player p = (Player) sender;
        p.chat("/warps");
        return true;
    }
}
