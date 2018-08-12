package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.APIManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class APIMessageListener implements Listener {
    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (event.isCancelled())
            return;

        if (!(event.getSender() instanceof Server))
            return;

        if (!event.getTag().equalsIgnoreCase(geSuit.CHANNEL_NAMES.API_CHANNEL.toString())) {
            return;
        }

		// Message debugging (can be toggled live)
		if (geSuit.instance.isDebugEnabled()) {
			Utilities.dumpPacket(event.getTag(), "SEND", event.getData(), true);
		}

        event.setCancelled(true);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        
        switch (task) {
        	case "UUIDToPlayerName":
        		// Convert a list of UUIDs to player names
        	    APIManager.doResolveIDs(((Server)event.getSender()).getInfo(), in.readInt(), in.readUTF());
        		break;
        	case "PlayerNameToUUID":
        		// Convert a list of player names to UUIDs
        	    APIManager.doResolveNames(((Server)event.getSender()).getInfo(), in.readInt(), in.readUTF());
        		break;
        	case "PlayerNameHistory":
        		// Fetch the history of player names for a specified UUID
        	    APIManager.doNameHistory(((Server)event.getSender()).getInfo(), in.readInt(), in.readUTF());
        		break;
        	default:
        		// Unknown API command
        		LoggingManager.log("WARNING: Unknown API command received: \"" + task + "\"!");
        		break;
        }
    }

}
