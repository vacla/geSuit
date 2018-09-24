package au.com.addstar.geSuitAdmin.listeners;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.utils.Utilities;
import au.com.addstar.geSuitAdmin.geSuitAdmin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import sun.rmi.runtime.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminListener implements PluginMessageListener, Listener{

    private geSuitAdmin instance;

    public AdminListener(geSuitAdmin instance) {
        this.instance = instance;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (BukkitModule.isDebug()) {
            instance.getLogger().info("DEBUG: " + Utilities.dumpPacket(channel,"RECV",message));
            instance.getLogger().info("Debug: this Server Name = " + instance.getName());
        }
        DataInputStream in = new DataInputStream( new ByteArrayInputStream( message ) );
        String task;
        try {
            task = in.readUTF();
            switch ( task ) {
                case "ServerRestart":
                    // servername sender milliSecs
                    String server = in.readUTF();
                    String sender = in.readUTF();
                    Long time = in.readLong();
                    if (instance.getName().equals(server)) {
                        instance.getServer().shutdown();
                        String timeString = Utilities.buildTimeDiffString(time, 4);
                        instance.getLogger().info("Shutdown issued by " + sender + " via " +
                                "geSuitAdmin" +
                                " in " + timeString);
                        instance.getServer().broadcastMessage(ChatColor.RED + " == THIS SERVER " +
                                "WILL RESTART IN  " + timeString + " THIS IS THE LAST WARNING...please" +
                                " move to another server now to avoid disconnection == ");
                        Bukkit.getScheduler().runTaskLater(instance, Bukkit::shutdown, time);
                    } else {
                        LoggingManager.debug("Debug: this Server Name = " + instance.getName());
                    }
                    break;
                case "EnableDebug":
                    instance.setDebug(!BukkitModule.isDebug());
                    LoggingManager.info("Debug : debug for this server is " + BukkitModule.isDebug());
                    break;
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
