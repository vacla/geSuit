package com.minecraftdimensions.bungeesuiteportals.managers;

import com.minecraftdimensions.bungeesuiteportals.BungeeSuitePortals;
import org.bukkit.entity.Player;

public class PermissionsManager {

    public static void addAllPermissions( Player player ) {
        player.addAttachment( BungeeSuitePortals.INSTANCE, "bungeesuite.portals.*", true );
    }

    public static void addAdminPermissions( Player player ) {
        player.addAttachment( BungeeSuitePortals.INSTANCE, "bungeesuite.portals.admin", true );
    }

    public static void addUserPermissions( Player player ) {
        player.addAttachment( BungeeSuitePortals.INSTANCE, "bungeesuite.portals.user", true );
    }
}
