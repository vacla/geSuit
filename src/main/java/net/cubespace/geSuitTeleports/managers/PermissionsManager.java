package net.cubespace.geSuitTeleports.managers;

import org.bukkit.entity.Player;

import net.cubespace.geSuitTeleports.BungeeSuiteTeleports;

public class PermissionsManager {
	
	public static void addAllPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.teleports.*", true);
	}
	public static void addAdminPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.teleports.admin", true);
	}
	public static void addUserPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.teleports.user", true);
	}
	public static void addVIPPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.teleports.vip", true);
	}
}
