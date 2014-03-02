package net.cubespace.geSuit;

import com.google.common.net.InetAddresses;
import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;
import net.md_5.bungee.api.ChatColor;

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
}
