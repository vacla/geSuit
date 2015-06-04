package net.cubespace.geSuit.teleports.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Homes;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.homes.HomeManager;
import net.cubespace.geSuit.teleports.misc.LocationUtil;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class HomeCommands {
    private HomeManager manager;
    
    public HomeCommands(HomeManager manager) {
        this.manager = manager;
    }
    
    @Command(name="sethome", async=true, permission="gesuit.homes.commands.sethome", description="Sets the players home location", usage="/<command> [<name>]")
    public void sethome(Player player, @Optional String home) {
        // TODO: These messages need to be configurable
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        Location location = LocationUtil.fromBukkit(player.getLocation(), Global.getServer().getName());
        
        boolean newHome = false;
        if (!manager.hasHome(gPlayer, home)) {
            // Check limits
            if (manager.getHomeCountServer(gPlayer) >= manager.getHomeLimitServer(player)) {
                player.sendMessage(ChatColor.RED + "You're not allowed to set anymore homes on this server.");
            }
            
            if (manager.getHomeCount(gPlayer) >= manager.getHomeLimitGlobal(player)) {
                player.sendMessage(ChatColor.RED + "You're not allowed to set anymore homes globally.");
            }
            
            newHome = true;
        }
        
        manager.setHome(gPlayer, home, location);
        
        if (newHome) {
            player.sendMessage(ChatColor.GOLD + "Your home \"" + home + "\" has been set");
        } else {
            player.sendMessage(ChatColor.GOLD + "Your home \"" + home + "\" has been updated");
        }
    }
    
    @Command(name="sethome", async=true, permission="gesuit.homes.commands.sethome", description="Sets the players home location", usage="/<command> <player> <name>")
    public void sethomeOther(Player player, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.sethome.other")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to do this");
            return;
        }
        
        // TODO: These messages need to be configurable
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Utilities.getPlayerAdvanced(playerName);
        if (gPlayer == null) {
            player.sendMessage(ChatColor.RED + "Unknown player " + playerName);
            return;
        }
        
        // Make sure the player is loaded
        if (!gPlayer.isLoaded()) {
            gPlayer.refresh();
        }
        
        Location location = LocationUtil.fromBukkit(player.getLocation(), Global.getServer().getName());
        
        boolean newHome = false;
        if (!manager.hasHome(gPlayer, home)) {
            newHome = true;
        }
        
        manager.setHome(gPlayer, home, location);
        
        if (newHome) {
            player.sendMessage(ChatColor.GOLD + "Home \"" + home + "\" has been set for " + gPlayer.getDisplayName());
        } else {
            player.sendMessage(ChatColor.GOLD + "Home \"" + home + "\" has been updated for " + gPlayer.getDisplayName());
        }
    }
    
    @Command(name="delhome", async=true, permission="gesuit.homes.commands.delhome", description="Deletes a players home", usage="/<command> [<name>]")
    public void delhome(Player player, @Optional String home) {
        // TODO: These messages need to be configurable
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        
        if (!manager.hasHome(gPlayer, home)) {
            player.sendMessage(ChatColor.RED + "That home does not exist");
            return;
        }
        
        manager.deleteHome(gPlayer, home);
        
        player.sendMessage(ChatColor.RED + "Your home \"" + home + "\" has been deleted");
    }
    
    @Command(name="delhome", async=true, permission="gesuit.homes.commands.delhome", description="Deletes a players home", usage="/<command> <player> <name>")
    public void delhome(Player player, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.delhome.other")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to do this");
            return;
        }
        // TODO: These messages need to be configurable
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Utilities.getPlayerAdvanced(playerName);
        if (gPlayer == null) {
            player.sendMessage(ChatColor.RED + "Unknown player " + playerName);
            return;
        }
        
        // Make sure the player is loaded
        if (!gPlayer.isLoaded()) {
            gPlayer.refresh();
        }
        
        if (!manager.hasHome(gPlayer, home)) {
            player.sendMessage(ChatColor.RED + "That home does not exist");
            return;
        }
        
        manager.deleteHome(gPlayer, home);
        
        player.sendMessage(ChatColor.RED + "Home \"" + home + "\" has been deleted for " + gPlayer.getDisplayName());
    }
    
    @Command(name="home", async=true, permission="gesuit.homes.commands.home", description="Teleports you home", usage="/<command> [<name>]")
    public void home(Player sender, @Optional String home) {
        // TODO: These messages need to be configurable
        GlobalPlayer gTarget;
        GlobalPlayer gPlayer = Global.getPlayer(sender.getUniqueId());
        if (home != null && home.contains(":")) {
            // Player and home specification
            if (!sender.hasPermission("gesuit.homes.commands.homes.other")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                return;
            }
            
            String[] parts = home.split(":");
            if (parts.length != 2) {
                sender.sendMessage(ChatColor.RED + "When specifying player name, use '<player>:<home>'");
                return;
            }
            
            gTarget = Utilities.getPlayerAdvanced(parts[0]);
            home = parts[1];
            
            if (gTarget == null) {
                sender.sendMessage(ChatColor.RED + "Unknown player " + parts[0]);
                return;
            }
            
            if (home.isEmpty()) {
                home = null;
            }
        } else {
            gTarget = gPlayer;
        }
        
        // When home is not specified, display the home list
        if (home == null) {
            displayHomes(sender, gTarget);
            return;
        }
        
        Location location = manager.getHome(gTarget, home);
        if (location == null) {
            sender.sendMessage(ChatColor.RED + "That home does not exist");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "You are being sent home");
        TeleportsModule.getTeleportManager().teleportWithDelay(gPlayer, location, TeleportCause.COMMAND);
    }
    
    @Command(name="homes", async=true, permission="gesuit.homes.commands.homes", description="Lists all of your homes", usage="/<command> [<player>]")
    public void homes(Player player, @Optional String playerName) {
        GlobalPlayer gTarget;
        if (playerName != null) {
            if (!player.hasPermission("gesuit.homes.commands.homes.other")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to do this");
                return;
            }
            
            gTarget = Utilities.getPlayerAdvanced(playerName);
        } else {
            gTarget = Global.getPlayer(player.getUniqueId());
        }
        
        displayHomes(player, gTarget); 
    }
    
    private void displayHomes(Player sender, GlobalPlayer player) {
        // TODO: These messages need to be configurable
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            if (player.getUniqueId().equals(sender.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "You do not have any homes set.");
            } else {
                sender.sendMessage(ChatColor.RED + "They do not have any homes set.");
            }
            return;
        }
        
        // Sort homes by server
        ListMultimap<String, String> homesByServer = ArrayListMultimap.create();
        for (Entry<String, Location> home : homes.getAllHomes().entrySet()) {
            homesByServer.put(home.getValue().getServer(), home.getKey());
        }
        
        // Header
        if (player.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Listing your homes:");
        } else {
            sender.sendMessage(ChatColor.RED + "Listing homes of " + player.getDisplayName() + ":");
        }
        
        // Display homes per server
        String thisServer = Global.getServer().getName();
        
        // Display this server first
        if (homesByServer.containsKey(thisServer)) {
            displayHomes(sender, homesByServer.get(thisServer), ChatColor.GREEN + thisServer + ": " + ChatColor.BLUE);
        }
        
        // Display all other servers/homes
        for (String server : homesByServer.keySet()) {
            if (server.equals(thisServer)) {
                continue;
            }
            
            displayHomes(sender, homesByServer.get(server), ChatColor.YELLOW + server + ": " + ChatColor.BLUE);
        }
    }
    
    private void displayHomes(Player sender, List<String> homes, String prefix) {
        Collections.sort(homes);
        
        StringBuilder builder = new StringBuilder(prefix);
        boolean first = true;
        for (String home : homes) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            
            builder.append(home);
        }
        
        sender.sendMessage(builder.toString());
    }
}
