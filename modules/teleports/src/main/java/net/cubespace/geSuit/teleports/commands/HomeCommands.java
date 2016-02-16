package net.cubespace.geSuit.teleports.commands;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Homes;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandTabCompleter;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.util.CustomPredicates;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.homes.HomeManager;
import net.cubespace.geSuit.teleports.misc.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class HomeCommands {
    private HomeManager manager;
    
    public HomeCommands(HomeManager manager) {
        this.manager = manager;
    }
    
    @Command(name="sethome", async=true, permission="gesuit.homes.commands.sethome", description="Sets the players home location", usage="/<command> [<name>]")
    public void sethome(Player player, @Optional String home) {
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        Location location = LocationUtil.fromBukkit(player.getLocation(), Global.getServer().getName());
        
        boolean newHome = false;
        if (!manager.hasHome(gPlayer, home)) {
            // Check limits
            if (manager.getHomeCountServer(gPlayer) >= manager.getHomeLimitServer(player)) {
                player.sendMessage(Global.getMessages().get("home.limit.server"));
            }
            
            if (manager.getHomeCount(gPlayer) >= manager.getHomeLimitGlobal(player)) {
                player.sendMessage(Global.getMessages().get("home.limit.global"));
            }
            
            newHome = true;
        }
        
        manager.setHome(gPlayer, home, location);
        
        if (newHome) {
            player.sendMessage(Global.getMessages().get("home.set", "home", home));
        } else {
            player.sendMessage(Global.getMessages().get("home.update", "home", home));
        }
    }
    
    @CommandTabCompleter(name="sethome")
    public Iterable<String> tabCompleteSetHome(Player player, int argument, String input, String home) {
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        Set<String> names = manager.getHomeNames(gPlayer);
        
        return Iterables.filter(names, CustomPredicates.startsWithCaseInsensitive(input));
    }
    
    @Command(name="sethome", async=true, permission="gesuit.homes.commands.sethome", description="Sets the players home location", usage="/<command> <player> <name>")
    public void sethomeOther(Player player, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.sethome.other")) {
            player.sendMessage(Global.getMessages().get("player.no-permission"));
            return;
        }
        
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Utilities.getPlayerAdvanced(playerName);
        if (gPlayer == null) {
            player.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
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
            player.sendMessage(Global.getMessages().get("home.set.other", "home", home, "player", gPlayer.getDisplayName()));
        } else {
            player.sendMessage(Global.getMessages().get("home.update.other", "home", home, "player", gPlayer.getDisplayName()));
        }
    }
    
    @CommandTabCompleter(name="sethome")
    public Iterable<String> tabCompleteSetHome(Player player, int argument, String input, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.sethome.other")) {
            return null;
        }
        
        switch (argument) {
        case 0: // playerName
            return Utilities.matchPlayerNames(input, true);
        case 1: // home
            GlobalPlayer gPlayer = Global.getPlayer(playerName);
            if (gPlayer != null) {
                Set<String> names = manager.getHomeNames(gPlayer);
                return Iterables.filter(names, CustomPredicates.startsWithCaseInsensitive(input));
            }
            break;
        }
        return null;
    }
    
    @Command(name="delhome", async=true, permission="gesuit.homes.commands.delhome", description="Deletes a players home", usage="/<command> [<name>]")
    public void delhome(Player player, @Optional String home) {
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        
        if (!manager.hasHome(gPlayer, home)) {
            player.sendMessage(Global.getMessages().get("home.unknown-home"));
            return;
        }
        
        manager.deleteHome(gPlayer, home);
        
        player.sendMessage(Global.getMessages().get("home.delete", "home", home));
    }
    
    @CommandTabCompleter(name="delhome")
    public Iterable<String> tabCompleteDelHome(Player player, int argument, String input, String home) {
        GlobalPlayer gPlayer = Global.getPlayer(player.getUniqueId());
        Set<String> names = manager.getHomeNames(gPlayer);
        
        return Iterables.filter(names, CustomPredicates.startsWithCaseInsensitive(input));
    }
    
    @Command(name="delhome", async=true, permission="gesuit.homes.commands.delhome", description="Deletes a players home", usage="/<command> <player> <name>")
    public void delhome(Player player, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.delhome.other")) {
            player.sendMessage(Global.getMessages().get("player.no-permission"));
            return;
        }
        if (home == null) {
            home = HomeManager.defaultHome;
        }
        
        GlobalPlayer gPlayer = Utilities.getPlayerAdvanced(playerName);
        if (gPlayer == null) {
            player.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        // Make sure the player is loaded
        if (!gPlayer.isLoaded()) {
            gPlayer.refresh();
        }
        
        if (!manager.hasHome(gPlayer, home)) {
            player.sendMessage(Global.getMessages().get("home.unknown-home"));
            return;
        }
        
        manager.deleteHome(gPlayer, home);
        
        player.sendMessage(Global.getMessages().get("home.delete.other", "home", home, "player", gPlayer.getDisplayName()));
    }
    
    @CommandTabCompleter(name="delhome")
    public Iterable<String> tabCompleteDelHome(Player player, int argument, String input, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.delhome.other")) {
            return null;
        }
        
        switch (argument) {
        case 0: // playerName
            return Utilities.matchPlayerNames(input, true);
        case 1: // home
            GlobalPlayer gPlayer = Global.getPlayer(playerName);
            if (gPlayer != null) {
                Set<String> names = manager.getHomeNames(gPlayer);
                return Iterables.filter(names, CustomPredicates.startsWithCaseInsensitive(input));
            }
            break;
        }
        return null;
    }
    
    @Command(name="home", async=true, permission="gesuit.homes.commands.home", description="Teleports you home", usage="/<command> [<name>]")
    public void home(Player sender, @Optional String home) {
        GlobalPlayer gTarget;
        GlobalPlayer gPlayer = Global.getPlayer(sender.getUniqueId());
        if (home != null && home.contains(":")) {
            // Player and home specification
            if (!sender.hasPermission("gesuit.homes.commands.homes.other")) {
                sender.sendMessage(Global.getMessages().get("player.no-permission"));
                return;
            }
            
            String[] parts = home.split(":");
            if (parts.length != 2) {
                sender.sendMessage(Global.getMessages().get("home.command.hint"));
                return;
            }
            
            gTarget = Utilities.getPlayerAdvanced(parts[0]);
            home = parts[1];
            
            if (gTarget == null) {
                sender.sendMessage(Global.getMessages().get("player.unknown", "player", parts[0]));
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
            sender.sendMessage(Global.getMessages().get("home.unknown-home"));
            return;
        }

        sender.sendMessage(Global.getMessages().get("home.teleport.other", "player", gTarget.getDisplayName(), "home", home));
        TeleportsModule.getTeleportManager().teleportWithDelay(gPlayer, location, TeleportCause.COMMAND);
    }
    
    @CommandTabCompleter(name="home")
    public Iterable<String> tabCompleteHome(Player player, int argument, String input, String home) {
        Iterable<String> homeNames;
        
        GlobalPlayer gPlayer;
        if (input.contains(":")) {
            if (!player.hasPermission("gesuit.homes.commands.homes.other")) {
                return null;
            }
            
            final String prefix = input.split(":")[0];
            gPlayer = Global.getPlayer(prefix);
            if (gPlayer == null) {
                return null;
            }
            
            homeNames = manager.getHomeNames(gPlayer);
            // Prepend prefix onto it
            homeNames = Iterables.transform(homeNames, new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return prefix + ":" + input;
                }
            });
        } else {
            gPlayer = Global.getPlayer(player.getUniqueId());
            homeNames = manager.getHomeNames(gPlayer);
        }
        
        return Iterables.filter(homeNames, CustomPredicates.startsWithCaseInsensitive(input));
    }
    
    @Command(name="homes", async=true, permission="gesuit.homes.commands.homes", description="Lists all of your homes", usage="/<command> [<player>]")
    public void homes(Player player, @Optional String playerName) {
        GlobalPlayer gTarget;
        if (playerName != null) {
            if (!player.hasPermission("gesuit.homes.commands.homes.other")) {
                player.sendMessage(Global.getMessages().get("player.no-permission"));
                return;
            }
            
            gTarget = Utilities.getPlayerAdvanced(playerName);
        } else {
            gTarget = Global.getPlayer(player.getUniqueId());
        }

        displayHomes(player, gTarget);
        if (playerName == null) { //only if the sender is asking for thier own homes - we cannot report on offline players.
            String servercount = manager.getHomeCountServer(gTarget) + "/" + manager.getHomeLimitServer(player);
            String globalCount = manager.getHomeCount(gTarget) + "/" + manager.getHomeLimitGlobal(player);
            player.sendMessage(Global.getMessages().get("home.list.footer", "servercount", servercount, "globalcount", globalCount));
        } else {
            String servercount = manager.getHomeCountServer(gTarget) + "/?";
            String globalCount = manager.getHomeCount(gTarget) + "/?";
            player.sendMessage(Global.getMessages().get("home.list.footer.other", "player", gTarget.getName(), "servercount", servercount, "globalcount", globalCount));
        }
    }
    
    @CommandTabCompleter(name="homes")
    public Iterable<String> tabCompleteHomes(Player player, int argument, String input, String playerName) {
        if (!player.hasPermission("gesuit.homes.commands.homes.other")) {
            return null;
        }
        
        switch (argument) {
        case 0: // playerName
            return Utilities.matchPlayerNames(input, true);
        }
        return null;
    }

    @Command(name = "sendhome", async = true, permission = "gesuit.homes.commands.sendhome", description = "Sends another player to default home or a selected home", usage = "/command <player> <homename>")
    public void sendHome(Player sender, String player, @Optional String home) {
        GlobalPlayer gPlayer;
        GlobalPlayer gTarget;
        gPlayer = Utilities.getPlayerAdvanced(player);
        if (gPlayer == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", player));
            return;
        }
        if (home != null && home.contains(":")) {
            // Player and home specification
            String[] parts = home.split(":");
            if (parts.length != 2) {
                sender.sendMessage(Global.getMessages().get("sendhome.command.hint"));
                return;
            }
            gTarget = Utilities.getPlayerAdvanced(parts[0]);
            home = parts[1];
            if (gTarget == null) {
                sender.sendMessage(Global.getMessages().get("player.unknown", "player", parts[0]));
                return;
            }
            if (home.isEmpty()) {
                home = null;
            }
        } else {
            gTarget = gPlayer;
        }
        if (home == null) {
            displayHomes(sender, gTarget);
            return;
        }
        Location location = manager.getHome(gTarget, home);
        if (location == null) {
            sender.sendMessage(Global.getMessages().get("home.unknown-home"));
            return;
        }
        sender.sendMessage(Global.getMessages().get("home.teleport", "home", home));
        TeleportsModule.getTeleportManager().teleportWithDelay(gPlayer, location, TeleportCause.COMMAND);
    }

    @CommandTabCompleter(name = "sendhome")
    public Iterable<String> tabCompleteSendHome(Player player, int argument, String input, String playerName, String home) {
        if (!player.hasPermission("gesuit.homes.commands.homes.other") || !player.hasPermission("gesuit.homes.commands.sendhome")) {
            return null;
        }
        switch (argument) {
            case 0: // playerName to send
                return Utilities.matchPlayerNames(input, true);
            case 1: //home format = targetPlayer:home OR homename
                Iterable<String> homeNames;
                GlobalPlayer gTarget;
                if (input.contains(":")) {
                    final String prefix = input.split(":")[0];
                    gTarget = Global.getPlayer(prefix);
                    if (gTarget == null) {
                        return null;
                    }
                    homeNames = manager.getHomeNames(gTarget);
                    // Prepend prefix onto it
                    homeNames = Iterables.transform(homeNames, new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            return prefix + ":" + input;
                        }
                    });
                } else {
                    gTarget = Global.getPlayer(playerName);
                    homeNames = manager.getHomeNames(gTarget);
                }
                return homeNames;
            default:
                return null;
        }
    }

    
    private void displayHomes(Player sender, GlobalPlayer player) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            if (player.getUniqueId().equals(sender.getUniqueId())) {
                sender.sendMessage(Global.getMessages().get("home.no-homes"));
            } else {
                sender.sendMessage(Global.getMessages().get("home.no-homes.other", "player", player.getDisplayName()));
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
            sender.sendMessage(Global.getMessages().get("home.list.header"));
        } else {
            sender.sendMessage(Global.getMessages().get("home.list.header.other", "player", player.getDisplayName()));
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
