package net.cubespace.geSuitTeleports.managers;

import net.cubespace.geSuitTeleports.geSuitTeleports;
import org.bukkit.entity.Player;

public class PermissionsManager {
	
	public static void addAllPermissions(Player player) {
		player.addAttachment(geSuitTeleports.instance, "gesuit.teleports.*", true);
	}
	public static void addAdminPermissions(Player player) {
		player.addAttachment(geSuitTeleports.instance, "gesuit.teleports.admin", true);
	}
	public static void addUserPermissions(Player player) {
		player.addAttachment(geSuitTeleports.instance, "gesuit.teleports.user", true);
	}
	public static void addVIPPermissions(Player player) {
		player.addAttachment(geSuitTeleports.instance, "gesuit.teleports.vip", true);
	}
}
