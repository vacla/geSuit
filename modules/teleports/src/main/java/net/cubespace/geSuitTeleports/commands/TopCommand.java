package net.cubespace.geSuitTeleports.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitTeleports.geSuitTeleports;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TopCommand extends CommandManager<TeleportsManager> {

    public TopCommand(TeleportsManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be a player to do this");
            return true;
        }
        
        Player player = (Player)sender;
        Location current = player.getLocation();
        Location location = new Location(current.getWorld(), current.getX(), current.getWorld().getMaxHeight(), current.getZ(), current.getYaw(), current.getPitch());
        player.teleport(manager.getUtil().getSafeDestination(location), TeleportCause.COMMAND);
        player.sendMessage(geSuitTeleports.tptop);
        return true;
    }
    
}
