package net.cubespace.geSuitPortals.managers;

import net.cubespace.geSuitPortals.geSuitPortals;
import org.bukkit.entity.Player;

public class PermissionsManager {

    public static void addAllPermissions(Player player) {
        player.addAttachment(geSuitPortals.getInstance(), "gesuit.portals.*", true);
    }

    public static void addAdminPermissions(Player player) {
        player.addAttachment(geSuitPortals.getInstance(), "gesuit.portals.admin", true);
    }

    public static void addUserPermissions(Player player) {
        player.addAttachment(geSuitPortals.getInstance(), "gesuit.portals.user", true);
    }
}
