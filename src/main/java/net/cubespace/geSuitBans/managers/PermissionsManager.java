package net.cubespace.geSuitBans.managers;

import net.cubespace.geSuitBans.geSuitBans;
import org.bukkit.entity.Player;

public class PermissionsManager {
	
	public static void addAllPermissions(Player player) {
		player.addAttachment(geSuitBans.instance, "gesuit.bans.*", true);
	}
	public static void addAdminPermissions(Player player) {
		player.addAttachment(geSuitBans.instance, "gesuit.bans.admin", true);
	}
	public static void addModPermissions(Player player) {
		player.addAttachment(geSuitBans.instance, "gesuit.bans.mod", true);
	}
}
