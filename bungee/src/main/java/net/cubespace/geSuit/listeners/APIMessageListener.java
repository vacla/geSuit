package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.APIManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class APIMessageListener extends MessageListener {

    public APIMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.API_CHANNEL);
    }
    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
		// Message debugging (can be toggled live)
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
