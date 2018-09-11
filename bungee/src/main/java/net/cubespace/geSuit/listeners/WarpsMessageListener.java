package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.WarpsManager;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class WarpsMessageListener extends MessageListener {

    public WarpsMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.WARP_CHANNEL);
    }

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();

        if (task.equals("WarpPlayer")) {
            //                            sender,       targetPlayer  warpName,     hasPermsForWarp,  hasPermsBypass
            WarpsManager.sendPlayerToWarp(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean());
            return;
        }

        if (task.equals("GetWarpsList")) {
            //                        sender        hasPermsServer,   hasPermsGlobal,   hasPermsHidden,   hasPermsBypass
            WarpsManager.getWarpsList(in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean());
            return;
        }

        if (task.equals("SetWarp")) {
            //                                           senderName,          warpName,                                                                    worldName,    X,               Y,               Z,               yaw,            pitch,           hidden,           global
            WarpsManager.setWarp(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), new Location(((Server) event.getSender()).getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean(), in.readBoolean());
            return;
        }

        if (task.equals("SetWarpDesc")) {
            //                                               senderName,          warpName,     description
            WarpsManager.setWarpDesc(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
            return;
        }

        if (task.equals("SilentWarpPlayer")) {
            //                            sender,       targetPlayer  warpName,     hasPermsForWarp,  hasPermsBypass,   showPlayerWarpedMessage
            WarpsManager.sendPlayerToWarp(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean(), false);
            return;
        }


        if (task.equals("DeleteWarp")) {
            //                                              senderName,          warpName
            WarpsManager.deleteWarp(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
            return;
        }

        if (task.equals("SendVersion")) {
            LoggingManager.log(in.readUTF());
        }

    }

}
