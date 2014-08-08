package net.cubespace.geSuit;

import com.google.common.net.InetAddresses;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.profile.Profile;
import net.cubespace.geSuit.tasks.DatabaseUpdateRowUUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

public class Utilities {
    public static boolean isIPAddress(String ip){
        return InetAddresses.isInetAddress(ip);
    }

    public static String colorize(String input) {
    	input = input.replace("{N}", "\n");
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static Map<String, String> getUUID(List<String> names) {
        try {
            Map uuids = Profile.getOnlineUUIDs(names);
            for (Map.Entry e : (Set<Map.Entry>) uuids.entrySet()) {
                e.setValue(e.getValue().toString().replace("-", ""));
            }
            return (Map<String,String>) uuids;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public static String getUUID(String name) {
        try {
            UUID id = Profile.getOnlineUUIDs(Collections.singletonList(name)).get(name);
            return id == null ? null : id.toString().replaceAll("-", "");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static void databaseUpdateRowUUID(int id, String playerName)
    {
        ProxyServer.getInstance().getScheduler().runAsync(geSuit.instance, new DatabaseUpdateRowUUID(id, playerName));
    }

    public static String dumpPacket(String channel, String direction, byte[] bytes, boolean consoleOutput) {
		String data = "";
		//ByteArrayInputStream ds = new ByteArrayInputStream(bytes);
		//DataInputStream di = new DataInputStream(ds);
		// Read upto 20 parameters from the stream and load them into the string list
		for (int x = 0; x < bytes.length; x++) {
			byte c = bytes[x];
			if (c >= 32 && c <= 126) {
				data += (char) c; 
			} else {
				data += "\\x" + Integer.toHexString(c);
			}
		}
		
		if (consoleOutput) {
			geSuit.instance.getLogger().info("geSuit DEBUG: [" + channel + "] " + direction + ": " + data);
		}
		return data;
	}
}
