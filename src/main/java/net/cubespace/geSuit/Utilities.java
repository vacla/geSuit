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
import java.util.concurrent.TimeUnit;

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
			geSuit.instance.getLogger().info("DEBUG: [" + channel + "] " + direction + ": " + data);
		}
		return data;
	}
    
    public static String buildTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();
        
        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Days ");
            } else {
                builder.append(" Day ");
            }
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Hours ");
            } else {
                builder.append(" Hour ");
            }
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Minutes ");
            } else {
                builder.append(" Minute ");
            }
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Seconds ");
            } else {
                builder.append(" Second ");
            }
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }
        
        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("< 1 Second");
        }

        return builder.toString().trim();
    }
    
    public static String buildShortTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();
        
        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("d ");
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("h ");
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("m ");
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("s ");
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }
        
        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("0s");
        }

        return builder.toString().trim();
    }
}
