package com.minecraftdimensions.bungeesuitewarps.managers;

import com.minecraftdimensions.bungeesuiteteleports.managers.TeleportsManager;
import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import com.minecraftdimensions.bungeesuitewarps.tasks.PluginMessageTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class WarpsManager {

    public static HashMap<String, Location> pendingWarps = new HashMap<>();

    public static void warpPlayer( CommandSender sender, String player, String warp ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "WarpPlayer" );
            out.writeUTF( sender.getName() );
            out.writeUTF( player );
            out.writeUTF( warp );
            out.writeBoolean( sender.hasPermission( "bungeesuite.warps.warp." + warp.toLowerCase() ) || sender.hasPermission( "bungeesuite.warps.warp.*" ) );
            out.writeBoolean( sender.hasPermission( "bungeesuite.warps.bypass" ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteWarps.instance );
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
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteWarps.instance );
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
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteWarps.instance );
    }


    public static void listWarps( CommandSender sender ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "GetWarpsList" );
            out.writeUTF( sender.getName() );
            out.writeBoolean( sender.hasPermission( "bungeesuite.warps.list.server" ) );
            out.writeBoolean( sender.hasPermission( "bungeesuite.warps.list.global" ) );
            out.writeBoolean( sender.hasPermission( "bungeesuite.warps.list.hidden" ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteWarps.instance );
    }

    public static void teleportPlayerToWarp( final String player, Location location ) {
        Player p = Bukkit.getPlayer( player );
        pendingWarps.put( player, location );
        if ( BungeeSuiteWarps.usingTeleports ) {
            TeleportsManager.ignoreTeleport.add( p );
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously( BungeeSuiteWarps.instance, new Runnable() {
            @Override
            public void run() {
                if ( pendingWarps.containsKey( player ) ) {
                    pendingWarps.remove( player );
                }
            }
        }, 100 );
    }


}