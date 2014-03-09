package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class BansMessageListener implements Listener {

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException, SQLException {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getSender() instanceof Server)) {
            return;
        }

        if (!event.getTag().equalsIgnoreCase("geSuitBans")) {
            return;
        }

        event.setCancelled(true);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        if (task.equals("KickPlayer")) {
            BansManager.kickPlayer(in.readUTF(), in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("BanPlayer")) {
            BansManager.banPlayer(in.readUTF(), in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("TempBanPlayer")) {
            BansManager.tempBanPlayer(in.readUTF(), in.readUTF(), in.readInt(), in.readUTF());
            return;
        }
        if (task.equals("KickAll")) {
            BansManager.kickAll(in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("UnbanPlayer")) {
            BansManager.unbanPlayer(in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("IPBanPlayer")) {
            BansManager.banIP(in.readUTF(), in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("CheckPlayerBans")) {
            BansManager.checkPlayersBan(in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("ReloadBans")) {
            BansManager.reloadBans(in.readUTF());
            return;
        }
        if (task.equals("SendVersion")) {
            LoggingManager.log(in.readUTF());
        }
    }

}
