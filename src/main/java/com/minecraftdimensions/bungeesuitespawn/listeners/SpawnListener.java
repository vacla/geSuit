package com.minecraftdimensions.bungeesuitespawn.listeners;

import com.minecraftdimensions.bungeesuitespawn.BungeeSuiteSpawn;
import com.minecraftdimensions.bungeesuitespawn.managers.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnListener implements Listener {

    @EventHandler( priority = EventPriority.LOWEST )
    public void playerLogin( PlayerJoinEvent e ) {
        if ( SpawnManager.pendingTeleports.containsKey( e.getPlayer().getName() ) ) {
            Location l = SpawnManager.pendingTeleports.get( e.getPlayer().getName() );
            e.getPlayer().teleport( l );
            return;
        }
        if ( !SpawnManager.HAS_SPAWNS ) {
            Bukkit.getScheduler().runTaskLaterAsynchronously( BungeeSuiteSpawn.INSTANCE, new Runnable() {
                @Override
                public void run() {
                    if ( !SpawnManager.HAS_SPAWNS ) {
                        SpawnManager.getSpawns();
                        SpawnManager.HAS_SPAWNS = true;
                    }
                }


            }, 10L );
        }
        //potential error below TODO
        Player p = e.getPlayer();
        if ( !p.hasPlayedBefore() ) {
            if ( SpawnManager.hasWorldSpawn( p.getWorld() ) && p.hasPermission( "bungeesuite.spawns.new.world" ) ) {
                SpawnManager.sendPlayerToWorldSpawn( p );
            } else if ( SpawnManager.hasServerSpawn() && p.hasPermission( "bungeesuite.spawns.new.server" ) ) {
                SpawnManager.sendPlayerToServerSpawn( p );
            } else if ( SpawnManager.hasGlobalSpawn() && p.hasPermission( "bungeesuite.spawns.new.global" ) ) {
                SpawnManager.sendPlayerToProxySpawn( p, true );
            }
        }

    }

    @EventHandler( priority = EventPriority.LOWEST )
    public void playerRespawn( PlayerRespawnEvent e ) {
        Player p = e.getPlayer();
        if ( p.getBedSpawnLocation() != null && p.hasPermission( "bungeesuite.spawns.spawn.bed" ) ) {
            e.setRespawnLocation( p.getBedSpawnLocation() );
        } else if ( SpawnManager.hasWorldSpawn( p.getWorld() ) && p.hasPermission( "bungeesuite.spawns.spawn.world" ) ) {
            e.setRespawnLocation( SpawnManager.getWorldSpawn( p.getWorld() ) );
        } else if ( SpawnManager.hasServerSpawn() && p.hasPermission( "bungeesuite.spawns.spawn.server" ) ) {
            e.setRespawnLocation( SpawnManager.getServerSpawn() );
        } else if ( SpawnManager.hasGlobalSpawn() && p.hasPermission( "bungeesuite.spawns.spawn.global" ) ) {
            if ( SpawnManager.hasWorldSpawn( p.getWorld() ) ) {
                e.setRespawnLocation( SpawnManager.getWorldSpawn( p.getWorld() ) );
            } else if ( SpawnManager.hasServerSpawn() ) {
                e.setRespawnLocation( SpawnManager.getServerSpawn() );
            }
            SpawnManager.sendPlayerToProxySpawn( p, true );
        }
    }

}
