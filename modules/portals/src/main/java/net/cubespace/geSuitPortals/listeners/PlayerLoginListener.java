package net.cubespace.geSuitPortals.listeners;

import net.cubespace.geSuitPortals.geSuitPortals;
import net.cubespace.geSuitPortals.managers.PermissionsManager;
import net.cubespace.geSuitPortals.managers.PortalsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;


public class PlayerLoginListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerConnect(PlayerJoinEvent e) {
        if (!PortalsManager.RECEIVED) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(geSuitPortals.instance, new Runnable() {

                @Override
                public void run() {
                    if (!PortalsManager.RECEIVED) {
                        PortalsManager.RECEIVED = true;
                        PortalsManager.requestPortals();
                    }

                }
            }, 10L);
        }
        if (PortalsManager.pendingTeleports.containsKey(e.getPlayer().getName())) {
            Location l = PortalsManager.pendingTeleports.get(e.getPlayer().getName());
            PortalsManager.pendingTeleports.remove(e.getPlayer().getName());
            e.getPlayer().teleport(l);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void setPermissionGroup(final PlayerLoginEvent e) {
        if (e.getPlayer().hasPermission("gesuit.*")) {
            PermissionsManager.addAllPermissions(e.getPlayer());
        } else if (e.getPlayer().hasPermission("gesuit.admin")) {
            PermissionsManager.addAdminPermissions(e.getPlayer());
        } else if (e.getPlayer().hasPermission("gesuit.user")) {
            PermissionsManager.addUserPermissions(e.getPlayer());
        }
    }

}
