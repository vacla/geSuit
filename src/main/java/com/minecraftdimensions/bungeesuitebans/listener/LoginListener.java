package com.minecraftdimensions.bungeesuitebans.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.minecraftdimensions.bungeesuitebans.managers.PermissionsManager;

public class LoginListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void setFormatChat(final PlayerLoginEvent e) {
		if(e.getPlayer().hasPermission("bungeesuite.*")){
			PermissionsManager.addAllPermissions(e.getPlayer());
		}else if(e.getPlayer().hasPermission("bungeesuite.admin")){
			PermissionsManager.addAdminPermissions(e.getPlayer());
		}else if(e.getPlayer().hasPermission("bungeesuite.mod")){
			PermissionsManager.addModPermissions(e.getPlayer());
		}
	}

	

}
