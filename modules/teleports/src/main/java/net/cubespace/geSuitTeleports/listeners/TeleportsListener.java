package net.cubespace.geSuitTeleports.listeners;

import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuitTeleports.geSuitTeleports;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class TeleportsListener implements Listener {

    private geSuitTeleports instance;
    private TeleportsManager manager;
    private SpawnManager spawnsManager;

    public TeleportsListener(TeleportsManager manager, SpawnManager spawnManager, geSuitTeleports pl) {
		super();
        this.manager = manager;
        instance = pl;
        spawnsManager = spawnManager;
	}
	
	@EventHandler
	public void playerConnect (PlayerSpawnLocationEvent e){
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
		if(TeleportsManager.pendingTeleports.containsKey(e.getPlayer().getName())){
			Player t = TeleportsManager.pendingTeleports.get(e.getPlayer().getName());
			TeleportsManager.pendingTeleports.remove(e.getPlayer().getName());
			if ((t == null) || (!t.isOnline())) {
				e.getPlayer().sendMessage(geSuitTeleports.no_longer_online);
				return;
			}
			TeleportsManager.ignoreTeleport.add(e.getPlayer());
			Location loc = t.getLocation();
            if (manager.getUtil().worldGuardTpAllowed(loc, e.getPlayer())) {
				e.setSpawnLocation(loc);
			}else{
				LoggingManager.debug(e.getPlayer().getName() + "being sent to spawn due to a worldguard block at " + loc.toString());
				e.setSpawnLocation(e.getPlayer().getWorld().getSpawnLocation());
			}
		}else if (TeleportsManager.pendingTeleportLocations.containsKey(e.getPlayer().getName())){
			Location l = TeleportsManager.pendingTeleportLocations.get(e.getPlayer().getName());
			TeleportsManager.ignoreTeleport.add(e.getPlayer());
            if (manager.getUtil().worldGuardTpAllowed(l, e.getPlayer())) {
				e.setSpawnLocation(l);
			}else{
				if((geSuitTeleports.geSuitSpawns) && (SpawnManager.hasWorldSpawn(e.getSpawnLocation().getWorld()))){
                    spawnsManager.sendPlayerToWorldSpawn(e.getPlayer());
				}else{
					e.setSpawnLocation(e.getSpawnLocation().getWorld().getSpawnLocation());
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent e){
        if (!manager.getUtil().worldGuardTpAllowed(e.getTo(), e.getPlayer())) { //cancel the event if the location is blocked
			e.setCancelled(true);
			e.setTo(e.getFrom());
			return;
		}
		if(e.getCause() != TeleportCause.PLUGIN && e.getCause() != TeleportCause.COMMAND){
			return;
		}
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
		
		if(TeleportsManager.ignoreTeleport.contains(e.getPlayer())){
			TeleportsManager.ignoreTeleport.remove(e.getPlayer());
			return;
		}
        manager.sendTeleportBackLocation(e.getPlayer(), false);
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent e){
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
		TeleportsManager.RemovePlayer(e.getPlayer());
		boolean empty = false;
		if(Bukkit.getOnlinePlayers().size() == 1){
			empty = true;
		}
        manager.sendTeleportBackLocation(e.getPlayer(), empty);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void playerDeath(PlayerDeathEvent e){
		if (e.getEntity().hasMetadata("NPC")) return; // Ignore NPCs
        manager.sendDeathBackLocation(e.getEntity());
        TeleportsManager.ignoreTeleport.add(e.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(final PlayerJoinEvent event) {
		if (event.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
	    // This is to prevent recording the back location when teleporting across servers, on the destination server
        TeleportsManager.ignoreTeleport.add(event.getPlayer());
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> TeleportsManager.ignoreTeleport.remove(event.getPlayer()), 20);
	}

}
