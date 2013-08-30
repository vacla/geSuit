package com.minecraftdimensions.bungeesuitewarps.managers;

import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import org.bukkit.entity.Player;

public class PermissionsManager {

    public static void addAllPermissions( Player player ) {
        player.addAttachment( BungeeSuiteWarps.instance, "bungeesuite.warps.*", true );
    }

    public static void addAdminPermissions( Player player ) {
        player.addAttachment( BungeeSuiteWarps.instance, "bungeesuite.warps.admin", true );
    }

    public static void addUserPermissions( Player player ) {
        player.addAttachment( BungeeSuiteWarps.instance, "bungeesuite.warps.user", true );
    }
}
