package net.cubespace.geSuitWarps.listeners;

import net.cubespace.geSuitWarps.managers.PermissionsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class WarpsListener implements Listener {
    @EventHandler( priority = EventPriority.NORMAL )
    public void setPermissionGroup( final PlayerLoginEvent e ) {
        if ( e.getPlayer().hasPermission( "gesuit.*" ) ) {
            PermissionsManager.addAllPermissions( e.getPlayer() );
        } else if ( e.getPlayer().hasPermission( "gesuit.admin" ) ) {
            PermissionsManager.addAdminPermissions( e.getPlayer() );
        } else if ( e.getPlayer().hasPermission( "gesuit.user" ) ) {
            PermissionsManager.addUserPermissions( e.getPlayer() );
        }
    }

}
