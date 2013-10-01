package com.minecraftdimensions.bungeesuiteportals.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;
import com.minecraftdimensions.bungeesuiteportals.objects.Portal;

public class PlayerMoveListener implements Listener {
	
	@EventHandler
	public void PlayerMove(PlayerMoveEvent e){
		Block t = e.getTo().getBlock();
		Block f = e.getFrom().getBlock();
		if(f.equals(t)){
			return;
		}
		if(!PortalsManager.PORTALS.containsKey(t.getWorld())){
			return;
		}
		
		for(Portal p: PortalsManager.PORTALS.get(t.getWorld())){
			if(p.isBlockInPortal(t)){
				if(e.getPlayer().hasPermission("bungeesuite.portals.portal.*")||e.getPlayer().hasPermission("bungeesuite.portals.portal."+p.getName())){
					PortalsManager.teleportPlayer(e.getPlayer(), p);
					Vector unitVector = e.getFrom().toVector().subtract(e.getTo().toVector()).normalize();
					Location l = e.getPlayer().getLocation();
					l.setYaw(l.getYaw()+180);
					e.getPlayer().teleport(l);
					e.getPlayer().setVelocity(unitVector.multiply(0.3));
				}
			}
		}

	}

}
