package com.minecraftdimensions.bungeesuitebans.managers;

import org.bukkit.entity.Player;

import com.minecraftdimensions.bungeesuitebans.BungeeSuiteBans;

public class PermissionsManager {
	
	public static void addAllPermissions(Player player) {
		player.addAttachment(BungeeSuiteBans.instance, "bungeesuite.bans.*", true);
	}
	public static void addAdminPermissions(Player player) {
		player.addAttachment(BungeeSuiteBans.instance, "bungeesuite.bans.admin", true);
	}
	public static void addModPermissions(Player player) {
		player.addAttachment(BungeeSuiteBans.instance, "bungeesuite.bans.mod", true);
	}
//	public static void addUserPermissions(Player player) {
//		player.addAttachment(BungeeSuiteBans.instance, "bungeesuite.chat.user", true);
//	}
//	public static void addVIPPermissions(Player player) {
//		player.addAttachment(BungeeSuiteBans.instance, "bungeesuite.chat.vip", true);
//	}
}
