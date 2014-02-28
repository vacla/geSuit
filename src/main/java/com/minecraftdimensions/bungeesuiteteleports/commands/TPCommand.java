package com.minecraftdimensions.bungeesuiteteleports.commands;


import com.minecraftdimensions.bungeesuiteteleports.managers.TeleportsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCommand implements CommandExecutor {

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
        if ( !( sender instanceof Player ) ) {
            if ( args.length == 2 ) {
                Bukkit.getPlayer( args[0] ).teleport( Bukkit.getPlayer( args[1] ) );
            } else if ( args.length == 4 ) {
                Player p = Bukkit.getPlayer( args[0] );
                p.teleport( new Location( p.getWorld(), Double.parseDouble( args[1] ), Double.parseDouble( args[2] ), Double.parseDouble( args[3] ) ) );
            }
        }
        if ( args.length == 1 ) {
            TeleportsManager.teleportToPlayer( sender, sender.getName(), args[0] );
            return true;
        } else if ( args.length == 2 ) {
            TeleportsManager.teleportToPlayer( sender, args[0], args[1] );
            return true;
        }
        if ( args.length == 3 ) {
            TeleportsManager.teleportToLocation( sender.getName(), ( ( Player ) sender ).getWorld().getName(), Double.valueOf( args[0] ), Double.valueOf( args[1] ), Double.valueOf( args[2] ) );
            return true;
        }
        if ( args.length == 5 ) {
            if ( Bukkit.getPlayer( args[0] ) != null ) {
                TeleportsManager.teleportToLocation( args[0], args[4], Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
            }
            return true;
        }

        return false;
    }

}
