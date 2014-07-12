package net.cubespace.geSuit;

import com.google.common.net.InetAddresses;
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
}
