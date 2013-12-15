package com.minecraftdimensions.bungeesuitespawn.managers;

import com.minecraftdimensions.bungeesuitespawn.BungeeSuiteSpawn;
import com.minecraftdimensions.bungeesuitespawn.tasks.PluginMessageTask;
import com.minecraftdimensions.bungeesuiteteleports.managers.TeleportsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class SpawnManager {

    public static boolean HAS_SPAWNS = false;
    public static HashMap<String, Location> SPAWNS = new HashMap<>();
    public static HashMap<String, Location> pendingTeleports = new HashMap<>();

    public static void sendPlayerToProxySpawn( CommandSender sender, boolean silent ) {

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SendToProxySpawn" );
            out.writeUTF( sender.getName() );
            out.writeBoolean( silent );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );

    }

    public static void setNewPlayerSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SetNewPlayerSpawn" );
            out.writeUTF( sender.getName() );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );

    }

    public static void setProxySpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SetProxySpawn" );
            out.writeUTF( sender.getName() );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );
    }

    public static void setServerSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();
        p.getWorld().setSpawnLocation( l.getBlockX(), l.getBlockY(), l.getBlockZ() );
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SetServerSpawn" );
            out.writeUTF( sender.getName() );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );
            out.writeBoolean( hasServerSpawn() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );

    }

    public static void setWorldSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();
        p.getWorld().setSpawnLocation( l.getBlockX(), l.getBlockY(), l.getBlockZ() );
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SetWorldSpawn" );
            out.writeUTF( sender.getName() );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );
            out.writeBoolean( hasWorldSpawn( p.getWorld() ) );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );

    }

    public static void sendPlayerToServerSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        p.teleport( getServerSpawn() );
    }

    public static void sendPlayerToWorldSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        Location l = getWorldSpawn( p.getWorld() );
        if ( l == null ) {
            p.teleport( p.getWorld().getSpawnLocation() );
        } else {
            p.teleport( getWorldSpawn( p.getWorld() ) );
        }
    }

    public static void getSpawns() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "GetSpawns" );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );

    }

    public static boolean hasWorldSpawn( World w ) {
        return SPAWNS.containsKey( w.getName() );
    }

    public static Location getWorldSpawn( World w ) {
        return SPAWNS.get( w.getName() );
    }

    public static boolean hasServerSpawn() {
        return SPAWNS.containsKey( "server" );
    }

    public static Location getServerSpawn() {
        return SPAWNS.get( "server" );
    }

    public static void sendPlayerToSpawn( CommandSender sender ) {
        Player p = ( Player ) sender;
        if ( SpawnManager.hasWorldSpawn( p.getWorld() ) && p.hasPermission( "bungeesuite.spawns.spawn.world" ) ) {
            p.teleport( getWorldSpawn( p.getWorld() ) );
        } else if ( SpawnManager.hasServerSpawn() && p.hasPermission( "bungeesuite.spawns.spawn.server" ) ) {
            p.teleport( getServerSpawn() );
        } else if ( p.hasPermission( "bungeesuite.spawns.spawn.global" ) ) {
            SpawnManager.sendPlayerToProxySpawn( p, false );
        }
    }

    public static void addSpawn( String name, String world, double x, double y, double z, float yaw, float pitch ) {
        SPAWNS.put( name, new Location( Bukkit.getWorld( world ), x, y, z, yaw, pitch ) );

    }

    public static void teleportPlayer( final String player, String world, double x, double y, double z, float yaw, float pitch ) {
        Location location = new Location( Bukkit.getWorld( world ), x, y, z, yaw, pitch );
        Player p = Bukkit.getPlayer( player );
        if ( p != null ) {
            p.teleport( location );
        } else {
            pendingTeleports.put( player, location );
            if ( BungeeSuiteSpawn.usingTeleports ) {
                TeleportsManager.ignoreTeleport.add( p );
            }
            Bukkit.getScheduler().runTaskLaterAsynchronously( BungeeSuiteSpawn.INSTANCE, new Runnable() {
                @Override
                public void run() {
                    if ( pendingTeleports.containsKey( player ) ) {
                        pendingTeleports.remove( player );
                    }
                }
            }, 100 );
        }

    }


    public static void sendVersion() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SendVersion" );
            out.writeUTF( ChatColor.RED + "Spawns - " + ChatColor.GOLD + BungeeSuiteSpawn.INSTANCE.getDescription().getVersion() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteSpawn.INSTANCE );
    }
}
