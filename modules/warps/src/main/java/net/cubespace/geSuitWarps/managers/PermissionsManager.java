package net.cubespace.geSuitWarps.managers;

import net.cubespace.geSuitWarps.geSuitWarps;
import org.bukkit.entity.Player;

public class PermissionsManager {

    public static void addAllPermissions( Player player ) {
        player.addAttachment( geSuitWarps.instance, "gesuit.warps.*", true );
    }

    public static void addAdminPermissions( Player player ) {
        player.addAttachment( geSuitWarps.instance, "gesuit.warps.admin", true );
    }

    public static void addUserPermissions( Player player ) {
        player.addAttachment( geSuitWarps.instance, "gesuit.warps.user", true );
    }
}
