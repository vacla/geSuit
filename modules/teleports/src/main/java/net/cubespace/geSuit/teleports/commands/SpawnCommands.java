package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.teleports.spawns.SpawnManager;
import net.cubespace.geSuit.teleports.spawns.SpawnType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpawnCommands {
    private SpawnManager manager;
    
    public SpawnCommands(SpawnManager manager) {
        this.manager = manager;
    }
    
    @Command(name="setnewspawn", async=true, aliases={"sns", "setnewplayerspawn", "setnoobspawn"}, permission="gesuit.spawns.command.setnewspawn", description="Sets the new players spawn point", usage="/<command>")
    public void setnewspawn(Player player) {
        manager.updateSpawn(SpawnType.NewPlayer, player.getLocation());
        player.sendMessage(ChatColor.GOLD + "New Player spawn point set");
    }
    
    @Command(name="setworldspawn", async=true, aliases={"sws"}, permission="gesuit.spawns.command.setworldspawn", description="Sets the world spawn point", usage="/<command>")
    public void setworldspawn(Player player) {
        manager.updateSpawn(SpawnType.World, player.getLocation());
        player.sendMessage(ChatColor.GOLD + "World spawn point set");
    }
    
    @Command(name="setserverspawn", async=true, aliases={"sss"}, permission="gesuit.spawns.command.setserverspawn", description="Sets the servers spawn point", usage="/<command>")
    public void setserverspawn(Player player) {
        manager.updateSpawn(SpawnType.Server, player.getLocation());
        player.sendMessage(ChatColor.GOLD + "Server spawn point set");
    }
    
    @Command(name="setglobalspawn", async=true, aliases={"sgs", "setproxyspawn", "sethub", "sethubspawn"}, permission="gesuit.spawns.command.setglobalspawn", description="Sets the global spawn point", usage="/<command>")
    public void setglobalspawn(Player player) {
        manager.updateSpawn(SpawnType.Global, player.getLocation());
        player.sendMessage(ChatColor.GOLD + "Global spawn point set");
    }
    
    
    @Command(name="spawn", async=true, permission="gesuit.spawns.command.spawn", description="Sends the player to the relevent spawn", usage="/<command>")
    public void spawn(Player player) {
        SpawnType type;
        if (manager.isSet(SpawnType.World, player.getWorld()) && player.hasPermission("gesuit.spawns.spawn.world")) {
            type = SpawnType.World;
        } else if (manager.isSet(SpawnType.Server, null) && player.hasPermission("gesuit.spawns.spawn.server")) {
            type = SpawnType.Server;
        } else if (player.hasPermission("gesuit.spawns.spawn.global")) {
            type = SpawnType.Global;
        } else {
            player.sendMessage(ChatColor.RED + "You dont have permission to go to spawn from here");
            return;
        }
        
        if (!manager.teleportTo(type, player)) {
            player.sendMessage(ChatColor.GOLD + "Cannot teleport, spawn is not set");
        }
    }
    
    @Command(name="worldspawn", async=true, aliases={"ws"}, permission="gesuit.spawns.command.worldspawn", description="Sends the player to the worlds spawn", usage="/<command>")
    public void worldspawn(Player player) {
        if (!manager.teleportTo(SpawnType.World, player)) {
            player.sendMessage(ChatColor.GOLD + "Cannot teleport, spawn is not set");
        }
    }
    
    @Command(name="serverspawn", async=true, aliases={"ss"}, permission="gesuit.spawns.command.serverspawn", description="Sends the player to the servers spawn", usage="/<command>")
    public void serverspawn(Player player) {
        if (!manager.teleportTo(SpawnType.Server, player)) {
            player.sendMessage(ChatColor.GOLD + "Cannot teleport, spawn is not set");
        }
    }
    
    @Command(name="globalspawn", async=true, aliases={"gs", "hub", "lobby"}, permission="gesuit.spawns.command.globalspawn", description="Sends the player to the proxys spawn", usage="/<command>")
    public void globalspawn(Player player) {
        if (!manager.teleportTo(SpawnType.Global, player)) {
            player.sendMessage(ChatColor.GOLD + "Cannot teleport, spawn is not set");
        }
    }
}
