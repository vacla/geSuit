package net.cubespace.geSuitWarps.managers;

import net.cubespace.geSuitWarps.geSuitWarps;
import net.cubespace.geSuitWarps.tasks.PluginMessageTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class WarpsManager {

    static HashMap<Player, Location> lastLocation = new HashMap<>();

    public static void warpPlayer( final CommandSender sender, final String senderName, final String warp ) {
        final Player player = Bukkit.getPlayer(sender.getName());
        Location currentLocation = player.getLocation();
        lastLocation.put(player, player.getLocation());

        player.sendMessage("Teleportation in progress, don't move!");

        geSuitWarps.getInstance().getServer().getScheduler().runTaskLater(geSuitWarps.getInstance(), new Runnable() {
            @Override
            public void run() {
                Integer lL = lastLocation.get(player).hashCode();
                Integer cL = player.getLocation().hashCode();

                if (lL == cL) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    try {
                        out.writeUTF("WarpPlayer");
                        out.writeUTF(sender.getName());
                        out.writeUTF(senderName);
                        out.writeUTF(warp);
                        out.writeBoolean(sender.hasPermission("gesuit.warps.warp." + warp.toLowerCase()) || sender.hasPermission("gesuit.warps.warp.*"));
                        out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new PluginMessageTask(b).runTaskAsynchronously(geSuitWarps.instance);
                }
                else {
                    player.sendMessage("You moved, teleportation aborted!");
                }
            }
        }, 100L);


    }

    public static void setWarp( CommandSender sender, String name, boolean hidden, boolean global ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        Location l = ( ( Player ) sender ).getLocation();
        try {
            out.writeUTF( "SetWarp" );
            out.writeUTF( sender.getName() );
            out.writeUTF( name );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );
            out.writeBoolean( hidden );
            out.writeBoolean( global );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitWarps.instance );
    }

    public static void deleteWarp( CommandSender sender, String warp ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DeleteWarp" );
            out.writeUTF( sender.getName() );
            out.writeUTF( warp );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitWarps.instance );
    }


    public static void listWarps( CommandSender sender ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "GetWarpsList" );
            out.writeUTF( sender.getName() );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.server" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.global" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.hidden" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.bypass" ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitWarps.instance );
    }
}