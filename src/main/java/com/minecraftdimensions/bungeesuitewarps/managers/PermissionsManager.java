package com.minecraftdimensions.bungeesuitewarps.managers;

import org.bukkit.entity.Player;

import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;

public class PermissionsManager {
	
	public static void addAllPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.warps.*", true);
	}
	public static void addAdminPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.warps.admin", true);
	}
	public static void addUserPermissions(Player player) {
		player.addAttachment(BungeeSuiteTeleports.instance, "bungeesuite.warps.user", true);
	}
}
