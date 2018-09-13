package net.cubespace.geSuitPortals.listeners;

import net.cubespace.geSuitPortals.geSuitPortals;
import net.cubespace.geSuitPortals.managers.PortalsManager;
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
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        String task;

        try {
            task = in.readUTF();
            switch (task) {
                case "SendPortal":
                    PortalsManager.addPortal(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()));
                    break;
                case "DeletePortal":
                    PortalsManager.removePortal(in.readUTF());
                    break;
                case "GetVersion": {
                    String name = null;
                    try {
                        name = in.readUTF();
                    } catch (IOException e) {

                    }
                    if (name != null) {
                        Player p = Bukkit.getPlayer(name);
                        p.sendMessage(ChatColor.RED + "Portals - " + ChatColor.GOLD + geSuitPortals.instance.getDescription().getVersion());
                    }
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Portals - " + ChatColor.GOLD + geSuitPortals.instance.getDescription().getVersion());
                    PortalsManager.sendVersion();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
