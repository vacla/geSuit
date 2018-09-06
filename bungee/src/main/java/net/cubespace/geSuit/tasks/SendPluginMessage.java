package net.cubespace.geSuit.tasks;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;

public class SendPluginMessage implements Runnable {
	
	  private final String channel;
	    private final ByteArrayOutputStream bytes;
	    private final ServerInfo server;
	    
	    public SendPluginMessage(String channel, ServerInfo server, ByteArrayOutputStream bytes) {
	        this.channel = channel;
	        this.bytes = bytes;
	        this.server = server;
	    }

	    public void run() {
			// Message debugging (can be toggled live)
            if (geSuit.getInstance().isDebugEnabled()) {
				Utilities.dumpPacket(channel, "SEND", bytes.toByteArray(), true);
			}

	    	server.sendData(channel, bytes.toByteArray());
	    }


}
