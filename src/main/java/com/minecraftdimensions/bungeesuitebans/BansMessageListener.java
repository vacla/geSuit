package com.minecraftdimensions.bungeesuitebans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * User: Bloodsplat
 * Date: 13/10/13
 */
public class BansMessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived( String channel, Player player, byte[] message ) {
        DataInputStream in = new DataInputStream( new ByteArrayInputStream( message ) );
        String task;

        try {
            task = in.readUTF();
            if ( task.equals( "GetVersion" ) ) {
                Player p = Bukkit.getPlayer( in.readUTF() );
                p.sendMessage( ChatColor.RED + "Bans - " + ChatColor.GOLD + BungeeSuiteBans.instance.getDescription().getVersion() );
                Bukkit.getConsoleSender().sendMessage( ChatColor.RED + "Bans - " + ChatColor.GOLD + BungeeSuiteBans.instance.getDescription().getVersion() );
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
