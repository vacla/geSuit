package net.cubespace.geSuitTeleports.commands;


import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCommand implements CommandExecutor {

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

        if ( !( sender instanceof Player ) ) {

            /* Console Commands */

            if (args.length < 2 || args.length > 6) {
                return false;
            }

            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[0]);
                return true;
            }

            // tp Player1 Player2
            if ( args.length == 2 ) {
                // Send Player1 to Player2
                Player p2 = Bukkit.getPlayer(args[1]);
                if (p2 == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[1]);
                    return true;
                }
                sender.sendMessage(ChatColor.GRAY + "Sending " + p.getName() + " to " + p2.getName());
                p.saveData();
                p.teleport( p2 );
                return true;
            }

            // tp Player X Y Z
            if ( args.length == 4 ) {
                // Send player to specified coordinates
                if (!validCoordinates(sender, args, 1)) {
                    return true;
                }
                sender.sendMessage(ChatColor.GRAY + "Sending " + p.getName() + " to " + args[1] + " " + args[2] + " " + args[3]);
                p.saveData();
                p.teleport( new Location( p.getWorld(), Double.parseDouble( args[1] ), Double.parseDouble( args[2] ), Double.parseDouble( args[3] ) ) );
                return true;
            }

            // tp Player X Y Z World
            if ( args.length == 5 ) {
                // Send player to the given coordinates of the given world (on this server)
                if (!validCoordinates(sender, args, 1)) {
                    return true;
                }
                sender.sendMessage(ChatColor.GRAY + "Sending " + p.getName() + " to " + args[1] + " " + args[2] + " " + args[3] + " in world " + args[4]);
                p.saveData();
                TeleportsManager.teleportToLocation( p.getName(), "", args[4], Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
                return true;
            }

            // tp Player X Y Z World Server
            if ( args.length == 6 ) {
                // Send player to the given coordinates of the given server and world
                if (!validCoordinates(sender, args, 1)) {
                    return true;
                }
                sender.sendMessage(ChatColor.GRAY + "Sending " + p.getName() + " to " + args[1] + " " + args[2] + " " + args[3] + " in world " + args[4] + " on server " + args[5]);
                p.saveData();
                TeleportsManager.teleportToLocation( p.getName(), args[5], args[4], Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
                return true;
            }

            return false;
        }

        /* Player Commands */

        if (args.length < 1 || args.length > 5) {
            return false;
        }

        // tp Player
        if ( args.length == 1 ) {
            // Teleport yourself to another player
            Player p2 = Bukkit.getPlayer(args[0]);
            if (p2 == null) {
                sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[0]);
                return true;
            }

            Player p = Bukkit.getPlayer(sender.getName());
            p.saveData();
            TeleportsManager.teleportToPlayer( sender, p.getName(), p2.getName() );
            return true;

        }

        // tp Player1 Player2
        if ( args.length == 2 ) {
            // Send Player1 to Player2

            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[0]);
                return true;
            }

            Player p2 = Bukkit.getPlayer(args[1]);
            if (p2 == null) {
                sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[1]);
                return true;
            }

            p.saveData();
            TeleportsManager.teleportToPlayer( sender, p.getName(), p2.getName() );
            return true;
        }

        // tp X Y Z
        if ( args.length == 3 ) {
            // Teleport yourself to the specified coordinates (of this world)
            if (!validCoordinates(sender, args, 0)) {
                return true;
            }
            Player p = Bukkit.getPlayer(sender.getName());
            p.saveData();
            TeleportsManager.teleportToLocation(p.getName(), "", p.getWorld().getName(), Double.valueOf(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]));
            return true;
        }

        // tp Player X Y Z
        // tp X Y Z World
        if ( args.length == 4) {
            Player p2 = Bukkit.getPlayer( args[0] );
            if ( p2 != null ) {
                // Teleport another player to the given coordinates (of this world)
                if (!validCoordinates(sender, args, 1)) {
                    return true;
                }
                p2.saveData();
                TeleportsManager.teleportToLocation( p2.getName(), "", ((Player) sender).getWorld().getName(), Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
            } else {
                // Teleport yourself to the specified coordinates of the given world (on this server)
                if (!validCoordinates(sender, args, 0)) {
                    return true;
                }
                Player p = Bukkit.getPlayer(sender.getName());
                p.saveData();
                TeleportsManager.teleportToLocation(p.getName(), "", args[3], Double.valueOf(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]));
            }
            return true;
        }

        // tp Player X Y Z World
        // tp Server World X Y Z
        if ( args.length == 5 ) {
            Player p2 = Bukkit.getPlayer( args[0] );
            if ( p2 != null ) {
                // Teleport another player to the given coordinates of the given world (on this server)
                if (!validCoordinates(sender, args, 1)) {
                    return true;
                }
                p2.saveData();
                TeleportsManager.teleportToLocation( p2.getName(), "", args[4], Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
            } else {
                // Teleport yourself to the given coordinates on the given server and world
                if (!validCoordinates(sender, args, 2)) {
                    return true;
                }
                Player p = Bukkit.getPlayer(sender.getName());
                p.saveData();
                TeleportsManager.teleportToLocation( p.getName(), args[0], args[1], Double.valueOf( args[2] ), Double.valueOf( args[3] ), Double.valueOf( args[4] ) );
            }
            return true;
        }

        // tp Player X Y Z World Server
        if ( args.length == 6 ) {
            Player p2 = Bukkit.getPlayer( args[0] );
            if (p2 == null) {
                sender.sendMessage(ChatColor.RED + "Invalid username or player is offline: " + args[1]);
                return true;
            }

            // Teleport another player to the given coordinates of the given server and world
            if (!validCoordinates(sender, args, 1)) {
                return true;
            }
            p2.saveData();
            TeleportsManager.teleportToLocation( p2.getName(), args[5], args[4], Double.valueOf( args[1] ), Double.valueOf( args[2] ), Double.valueOf( args[3] ) );
        }

        return false;
    }

    private boolean validCoordinates(CommandSender sender, String[] args, int startIndex) {

        if (validCoordinate(sender, "X", args[startIndex]) &&
            validCoordinate(sender, "Y", args[startIndex + 1]) &&
            validCoordinate(sender, "Z", args[startIndex + 2])) {
            return true;
        }
        return false;
    }

    private boolean validCoordinate(CommandSender sender, String coordName, String coordValue) {

        if (!NumberUtils.isNumber(coordValue)) {
            sender.sendMessage(ChatColor.RED + "Invalid " + coordName + " coordinate: " + coordValue);
            return false;
        }

        return true;
    }

}
