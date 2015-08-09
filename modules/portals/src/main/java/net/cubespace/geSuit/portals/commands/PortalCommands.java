package net.cubespace.geSuit.portals.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandContext;
import net.cubespace.geSuit.core.commands.CommandTabCompleter;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Portal;
import net.cubespace.geSuit.core.objects.Warp;
import net.cubespace.geSuit.core.util.CustomPredicates;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.portals.PortalManager;
import net.cubespace.geSuit.teleports.WarpsModule;
import net.cubespace.geSuit.teleports.misc.LocationUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Functions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class PortalCommands {
    private PortalManager manager;
    private WorldEditPlugin worldEdit;
    
    public PortalCommands(PortalManager manager, WorldEditPlugin worldEdit) {
        this.manager = manager;
        this.worldEdit = worldEdit;
    }
    
    @Command(name="setportal", async=true, aliases={"createportal", "makeportal", "sportal"}, permission="gesuit.portals.command.setportal", usage="/<command> <name> <type> <destination> [<fill>]")
    public void setportal(CommandContext<Player> context, String portalName, Portal.Type type, String destination, @Optional Portal.FillType fill) {
        // Handle parse errors
        if (context.isErrored()) {
            switch (context.getErrorArg()) {
            case 1: // type
                context.setErrorMessage(Global.getMessages().get("portal.command.error.type"));
                break;
            case 3: // filltype
                context.setErrorMessage(Global.getMessages().get("portal.command.error.fill"));
                break;
            }
            return;
        }
        
        if (fill == null) {
            fill = Portal.FillType.Air;
        }
        
        Player player = context.getSender();
        
        Selection selection = worldEdit.getSelection(player);
        if (selection == null) {
            player.sendMessage(Global.getMessages().get("portal.command.error.no-sel"));
            return;
        }
        
        if (!(selection instanceof CuboidSelection)) {
            player.sendMessage(Global.getMessages().get("portal.command.error.sel-type"));
            return;
        }
        
        Location min = LocationUtil.fromBukkit(selection.getMinimumPoint(), Global.getServer().getName());
        Location max = LocationUtil.fromBukkit(selection.getMaximumPoint(), Global.getServer().getName());
        
        // Check destination and make portal
        Portal portal;
        switch (type) {
        case Server:
            if (Global.getServer(destination) == null) {
                player.sendMessage(Global.getMessages().get("portal.command.error.unknown-server", "server", destination));
                return;
            }
            
            portal = new Portal(portalName, type, destination, fill, min, max);
            break;
        case Warp:
            if (!WarpsModule.getWarpManager().hasWarp(destination)) {
                player.sendMessage(Global.getMessages().get("portal.command.error.unknown-warp", "warp", destination));
                return;
            }
            
            portal = new Portal(portalName, type, destination, fill, min, max);
            break;
        case Teleport: {
            String[] parts = destination.split(",");
            if (parts.length < 3 || parts.length > 6 || parts.length == 5) {
                player.sendMessage(Global.getMessages().get("portal.command.error.unknown-location", "location", destination, "format", "x,y,z x,y,z,world or x,y,z,world,yaw,pitch"));
                return;
            }
            
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                
                World world = player.getWorld();
                float yaw = 0;
                float pitch = 0;
                
                // With world defined
                if (parts.length >= 4) {
                    world = Bukkit.getWorld(parts[3]);
                    if (world == null) {
                        player.sendMessage(Global.getMessages().get("portal.command.error.unknown-world", parts[3]));
                        return;
                    }
                }
                
                // With yaw and pitch defined
                if (parts.length == 6) {
                    yaw = Float.parseFloat(parts[4]);
                    pitch = Float.parseFloat(parts[5]);
                }
                
                portal = new Portal(portalName, new Location(Global.getServer().getName(), world.getName(), x, y, z, yaw, pitch), fill, min, max);
            } catch (NumberFormatException e) {
                player.sendMessage(Global.getMessages().get("portal.command.error.unknown-location", "location", destination, "format", "x,y,z x,y,z,world or x,y,z,world,yaw,pitch"));
                return;
            }
            break;
        }
        default:
            throw new AssertionError();
        }
        
        if (manager.addOrUpdatePortal(portal)) {
            player.sendMessage(Global.getMessages().get("portal.create"));
        } else {
            player.sendMessage(Global.getMessages().get("portal.update"));
        }
    }
    
    @CommandTabCompleter(name="setportal")
    public Iterable<String> tabCompleterSetPortal(Player sender, int argument, String input, String portalName, Portal.Type type, String destination, Portal.FillType fill) {
        switch (argument) {
        case 0: // portalName
            return null;
        case 1: // type
            return Iterables.filter(Iterables.transform(Arrays.asList(Portal.Type.values()), Functions.toStringFunction()), CustomPredicates.startsWithCaseInsensitive(input));
        case 2: // destinationType
            switch (type) {
            case Server:
                return Utilities.matchServerNames(input);
            case Warp:
                input = input.toLowerCase();
                List<String> warps = Lists.newArrayList();
                for (Warp warp : WarpsModule.getWarpManager().getWarps()) {
                    if (warp.getName().toLowerCase().startsWith(input)) {
                        warps.add(warp.getName());
                    }
                }
                
                return warps;
            default:
                return null;
            }
        case 3: // fill
            return Iterables.filter(Iterables.transform(Arrays.asList(Portal.FillType.values()), Functions.toStringFunction()), CustomPredicates.startsWithCaseInsensitive(input));
        }
        
        return null;
    }
    
    @Command(name="delportal", async=true, aliases={"deleteportal", "dportal", "removeportal"}, permission="gesuit.portals.command.delportal", usage="/<command> <name>")
    public void delportal(Player player, String portalName) {
        if (!manager.hasPortal(portalName)) {
            player.sendMessage(Global.getMessages().get("portal.unknown-portal"));
            return;
        }
        
        manager.removePortal(portalName);
        player.sendMessage(Global.getMessages().get("portal.delete", "portal", portalName));
    }
    
    @CommandTabCompleter(name="delportal")
    public Iterable<String> tabCompleterDelPortal(Player sender, int argument, String input, String portalName) {
        input = input.toLowerCase();
        List<String> portals = Lists.newArrayList();
        for (Portal portal : manager.getPortals()) {
            if (portal.getName().toLowerCase().startsWith(input)) {
                portals.add(portal.getName());
            }
        }
        
        return portals;
    }
    
    @Command(name="portals", async=true, aliases={"portalslist", "portallist", "listportals"}, permission="gesuit.portals.command.portals", usage="/<command>")
    public void portals(CommandSender sender) {
        Collection<Portal> portals = manager.getPortals();
        
        if (portals.isEmpty()) {
            sender.sendMessage(Global.getMessages().get("portal.list.empty"));
            return;
        }
        
        // Group by server name
        ListMultimap<String, String> byServer = ArrayListMultimap.create();
        for (Portal portal : portals) {
            byServer.put(portal.getServer(), portal.getName().toLowerCase());
        }
        
        // Header
        sender.sendMessage(Global.getMessages().get("portal.list.header"));
        
        // Display portals per server
        String thisServer = Global.getServer().getName();
        
        // Display this server first
        if (byServer.containsKey(thisServer)) {
            displayPortals(sender, byServer.get(thisServer), ChatColor.GOLD + thisServer + ": " + ChatColor.RESET);
        }
        
        // Display all other servers/homes
        for (String server : byServer.keySet()) {
            if (server.equals(thisServer)) {
                continue;
            }
            
            displayPortals(sender, byServer.get(server), ChatColor.GOLD + server + ": " + ChatColor.RESET);
        }
    }
    
    private void displayPortals(CommandSender sender, List<String> portals, String prefix) {
        Collections.sort(portals);
        
        StringBuilder builder = new StringBuilder(prefix);
        boolean first = true;
        for (String portal : portals) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            
            builder.append(portal);
        }
        
        sender.sendMessage(builder.toString());
    }
    
    @Command(name="portalenable", permission="gesuit.portals.command.enableportal", usage="/<command> <portal> <true/false>")
    public void enablePortal(CommandSender sender, String portalName, boolean enabled) {
        if (!manager.hasPortal(portalName)) {
            sender.sendMessage(Global.getMessages().get("portal.unknown-portal"));
            return;
        }
        
        Portal portal = manager.getPortal(portalName);
        if (portal.isEnabled() == enabled) {
            if (enabled) {
                sender.sendMessage(Global.getMessages().get("portal.command.error.already-enabled", "name", portal.getName()));
            } else {
                sender.sendMessage(Global.getMessages().get("portal.command.error.already-disabled", "name", portal.getName()));
            }
        } else {
            portal.setEnabled(enabled);
            if (enabled) {
                sender.sendMessage(Global.getMessages().get("portal.enabled", "name", portal.getName()));
                manager.placePortal(portal);
            } else {
                sender.sendMessage(Global.getMessages().get("portal.disabled", "name", portal.getName()));
                manager.clearPortal(portal);
            }
        }
    }
    
    @CommandTabCompleter(name="portalenable")
    public Iterable<String> tabCompleterEnablePortal(CommandSender sender, int argument, String input, String portalName, boolean enabled) {
        if (argument == 0) { // portalName
            input = input.toLowerCase();
            List<String> portals = Lists.newArrayList();
            for (Portal portal : manager.getPortals()) {
                if (portal.getName().toLowerCase().startsWith(input)) {
                    portals.add(portal.getName());
                }
            }
            
            return portals;
        } else {
            return Iterables.filter(Arrays.asList("true", "false"), CustomPredicates.startsWithCaseInsensitive(input));
        }
    }
}
