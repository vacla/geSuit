package net.cubespace.geSuit.teleports.commands;

import java.util.List;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Warp;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.misc.LocationUtil;
import net.cubespace.geSuit.teleports.warps.WarpManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class WarpCommands {
    private WarpManager manager;
    
    public WarpCommands(WarpManager manager) {
        this.manager = manager;
    }
    
    @Command(name="warps", async=true, aliases={"warplist"}, permission="gesuit.warps.command.warps", description="Shows a list of available warps", usage="/<command>")
    public void warps(CommandSender sender) {
        boolean canSeeHidden = sender.hasPermission("gesuit.warps.list.hidden");
        
        if (sender.hasPermission("gesuit.warps.list.global")) {
            List<Warp> globalWarps = manager.getGlobalWarps(canSeeHidden);
            sender.sendMessage(ChatColor.GOLD + "Global warps:");
            sendWarpList(sender, globalWarps);
        }
        
        if (sender.hasPermission("gesuit.warps.list.server")) {
            List<Warp> localWarps = manager.getLocalWarps(canSeeHidden);
            sender.sendMessage(ChatColor.GOLD + "Server warps:");
            sendWarpList(sender, localWarps);
        }
    }
    
    private void sendWarpList(CommandSender sender, List<Warp> warps) {
        if (warps.isEmpty()) {
            sender.sendMessage(ChatColor.RED + " none");
            return;
        }
        
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        
        builder.append(ChatColor.YELLOW);
        builder.append(' ');
        
        for (Warp warp : warps) {
            if (!first) {
                builder.append(", ");
            }
            
            first = false;
            if (warp.isHidden()) {
                builder.append(ChatColor.GRAY);
                builder.append("[H]");
                builder.append(ChatColor.YELLOW);
            }
            
            builder.append(warp.getName());
        }
        
        sender.sendMessage(builder.toString());
    }
    
    @Command(name="warp", async=true, aliases={"warpto"}, permission="gesuit.warps.command.warp", description="Warps a player to a specific warp", usage="/<command> [player] <warp>")
    public void warp(CommandSender sender, @Optional String playerName, String warpName) {
        // TODO: Make messages configurable
        GlobalPlayer player;
        boolean warpSelf;
        
        // Determine player
        if (playerName == null) {
            // Consoles must provide player name
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must specify a player name when run from console");
                return;
            }
            player = Global.getPlayer(((Player)sender).getUniqueId());
            warpSelf = true;
        } else {
            if (!sender.hasPermission("gesuit.warps.command.warp.other")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this");
                return;
            }
            
            player = Global.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Unknown player " + playerName);
                return;
            }
            warpSelf = false;
        }
        
        Warp warp = manager.getWarp(warpName);
        if (warp == null) {
            sender.sendMessage(ChatColor.RED + "That warp does not exist");
            return;
        }
        
        // Check permission
        if (!sender.hasPermission("gesuit.warps.warp.*") && !sender.hasPermission("gesuit.warps.warp." + warpName.toLowerCase())) {
            if (warp.isHidden()) {
                // Actually hide the warp if they cant use it
                sender.sendMessage(ChatColor.RED + "That warp does not exist");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that warp");
            }
            return;
        }
        
        // Perform the warp
        if (warpSelf) {
            TeleportsModule.getTeleportManager().teleportWithDelay(player, warp.getLocation(), TeleportCause.COMMAND);
            sender.sendMessage(ChatColor.GRAY + "You have been warped to " + warpName);
        } else {        
            TeleportsModule.getTeleportManager().teleport(player, warp.getLocation(), TeleportCause.COMMAND);
            // TODO: Enable sending messages to players
            //player.sendMessage(ChatColor.GRAY + "You have been warped to " + warpName);
            sender.sendMessage(ChatColor.GRAY + player.getDisplayName() + " has been warped to " + warpName);
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name>")
    public void setwarp(Player sender, String warp) {
        // TODO: Allow messages to be customized
        boolean isUpdate = manager.hasGlobalWarp(warp);
        manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), false);
        
        if (isUpdate) {
            sender.sendMessage(ChatColor.GOLD + "Successfully updated the warp");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Successfully created a warp");
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false>")
    public void setwarp(Player sender, String warp, boolean hidden) {
        // TODO: Allow messages to be customized
        boolean isUpdate = manager.hasGlobalWarp(warp);
        manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        
        if (isUpdate) {
            sender.sendMessage(ChatColor.GOLD + "Successfully updated the warp");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Successfully created a warp");
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false> <global true/false>")
    public void setwarp(Player sender, String warp, boolean hidden, boolean global) {
        // TODO: Allow messages to be customized
        boolean isUpdate;
        if (global) {
            isUpdate = manager.hasGlobalWarp(warp);
            manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        } else {
            isUpdate = manager.hasLocalWarp(warp);
            manager.setLocalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        }
        
        if (isUpdate) {
            sender.sendMessage(ChatColor.GOLD + "Successfully updated the warp");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Successfully created a warp");
        }
    }
    
    @Command(name="delwarp", async=true, aliases={"deletewarp", "removewarp"}, permission="gesuit.warps.command.delwarp", description="Used to delete a specific warp", usage="/<command> <name>")
    public void delwarp(CommandSender sender, String name) {
        // TODO: Allow messages to be customized
        if (manager.hasLocalWarp(name)) {
            manager.removeLocalWarp(name);
        } else if (manager.hasGlobalWarp(name)) {
            manager.removeGlobalWarp(name);
        } else {
            sender.sendMessage(ChatColor.RED + "That warp does not exist");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "Successfully deleted the warp");
    }
}
