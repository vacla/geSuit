package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.PortalManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PortalsMessageListener extends MessageListener {

    public PortalsMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.PORTAL_CHANNEL);
    }

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (eventNotMatched(event)) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        Server s = (Server) event.getSender();
        switch (task) {
            case "TeleportPlayer":
                PortalManager.teleportPlayer(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF(), in.readBoolean());
                break;
            case "ListPortals":
                PortalManager.listPortals(PlayerManager.getPlayer(in.readUTF(), true));
                break;
            case "DeletePortal":
                PortalManager.deletePortal(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
                break;
            case "SetPortal":
                GSPlayer sender = PlayerManager.getPlayer(in.readUTF(), true);
                boolean selection = in.readBoolean();
                if (!selection) {
                    PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_SELECTION_MADE);
                } else {
                    PortalManager.setPortal(sender, in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), new Location(s.getInfo(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble()), new Location(s.getInfo(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble()));
                }
                break;
            case "RequestPortals":
                PortalManager.getPortals(s.getInfo());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
        }

        in.close();

    }

}
