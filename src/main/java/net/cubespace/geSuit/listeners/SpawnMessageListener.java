package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class SpawnMessageListener implements Listener {

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException, SQLException {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getSender() instanceof Server))
            return;
        if (!event.getTag().equalsIgnoreCase("geSuitSpawns")) {
            return;
        }

        // Message debugging (can be toggled live)
		if (geSuit.instance.isDebugEnabled()) {
			Utilities.dumpPacket(event.getTag(), "RECV", event.getData(), true);
		}

        event.setCancelled(true);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        Server s = (Server) event.getSender();

        switch (task) {
            case "SendToProxySpawn":
                SpawnManager.sendPlayerToProxySpawn(PlayerManager.getPlayer(in.readUTF(), true));
                break;
            case "GetSpawns":
                SpawnManager.sendSpawns(s);
                break;
            case "SetServerSpawn":
                SpawnManager.setServerSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(s.getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean());
                break;
            case "SetWorldSpawn":
                SpawnManager.setWorldSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(s.getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean());
                break;
            case "DelWorldSpawn":
                SpawnManager.delWorldSpawn(PlayerManager.getPlayer(in.readUTF(), true), s.getInfo(), in.readUTF());
                break;
            case "SetNewPlayerSpawn":
                SpawnManager.setNewPlayerSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(s.getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
            case "SetProxySpawn":
                SpawnManager.setProxySpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(s.getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
            case "SendToArgSpawn":
                SpawnManager.sendPlayerToArgSpawn(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
        }

        in.close();

    }

}
