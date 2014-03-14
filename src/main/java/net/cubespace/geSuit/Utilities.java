package net.cubespace.geSuit;

import com.google.common.net.InetAddresses;
import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;
import net.cubespace.geSuit.tasks.DatabaseUpdateRowUUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

public class Utilities {
    private static final HttpProfileRepository profileRepository = new HttpProfileRepository();

	public static boolean isIPAddress(String ip){
		return InetAddresses.isInetAddress(ip);
	}

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String getUUID(String name) {
        Profile[] profiles = profileRepository.findProfilesByCriteria(new ProfileCriteria(name, "minecraft"));

        if (profiles.length > 0) {
            return profiles[0].getId();
        } else {
            return null;
        }
    }
    
    public static void databaseUpdateRowUUID(int id, String playerName)
    {
        ProxyServer.getInstance().getScheduler().runAsync(geSuit.instance, new DatabaseUpdateRowUUID(id, playerName));
    }
}
