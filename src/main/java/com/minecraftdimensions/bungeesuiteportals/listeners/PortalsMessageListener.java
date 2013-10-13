package com.minecraftdimensions.bungeesuiteportals.listeners;

import com.minecraftdimensions.bungeesuiteportals.BungeeSuitePortals;
import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PortalsMessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived( String channel, Player player, byte[] message ) {
        DataInputStream in = new DataInputStream( new ByteArrayInputStream( message ) );
        String task;

        try {
            task = in.readUTF();
            switch ( task ) {
                case "TeleportPlayer": {
                    Player p = Bukkit.getPlayer( in.readUTF() );
                    Location l = new Location( Bukkit.getWorld( in.readUTF() ), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat() );
                    if ( p == null ) {
                        PortalsManager.pendingTeleports.put( p, l );
                    } else {
                        p.teleport( l );
                    }
                    break;
                }
                case "SendPortal":
                    PortalsManager.addPortal( in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), new Location( Bukkit.getWorld( in.readUTF() ), in.readDouble(), in.readDouble(), in.readDouble() ), new Location( Bukkit.getWorld( in.readUTF() ), in.readDouble(), in.readDouble(), in.readDouble() ) );
                    break;
                case "DeletePortal":
                    PortalsManager.removePortal( in.readUTF() );
                    break;
                case "GetVersion": {
                    Player p = Bukkit.getPlayer( in.readUTF() );
                    p.sendMessage( ChatColor.RED + "Portals - " + ChatColor.GOLD + BungeeSuitePortals.INSTANCE.getDescription().getVersion() );
                    Bukkit.getConsoleSender().sendMessage( ChatColor.RED + "Portals - " + ChatColor.GOLD + BungeeSuitePortals.INSTANCE.getDescription().getVersion() );
                    break;
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

}
