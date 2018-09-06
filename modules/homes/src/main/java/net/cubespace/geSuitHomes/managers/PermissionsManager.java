package net.cubespace.geSuitHomes.managers;

import org.bukkit.entity.Player;

import net.cubespace.geSuitHomes.geSuitHomes;

public class PermissionsManager {

    public static void addAllPermissions( Player player ) {
        player.addAttachment( geSuitHomes.instance, "gesuit.homes.*", true );
    }

    public static void addAdminPermissions( Player player ) {
        player.addAttachment( geSuitHomes.instance, "gesuit.homes.admin", true );
    }

    public static void addUserPermissions( Player player ) {
        player.addAttachment( geSuitHomes.instance, "gesuit.homes.user", true );
    }
}
