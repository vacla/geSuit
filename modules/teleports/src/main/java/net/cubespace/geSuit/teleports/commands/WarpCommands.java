package net.cubespace.geSuit.teleports.commands;

import java.util.List;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandTabCompleter;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Warp;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.teleports.TeleportsModule;
import net.cubespace.geSuit.teleports.misc.LocationUtil;
import net.cubespace.geSuit.teleports.warps.WarpManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.collect.Lists;

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
            sender.sendMessage(Global.getMessages().get("warp.list.global"));
            sendWarpList(sender, globalWarps);
        }
        
        if (sender.hasPermission("gesuit.warps.list.server")) {
            List<Warp> localWarps = manager.getLocalWarps(canSeeHidden);
            sender.sendMessage(Global.getMessages().get("warp.list.server"));
            sendWarpList(sender, localWarps);
        }
    }
    
    private void sendWarpList(CommandSender sender, List<Warp> warps) {
        if (warps.isEmpty()) {
            sender.sendMessage(" " + Global.getMessages().get("warp.list.empty"));
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
        GlobalPlayer player;
        boolean warpSelf;
        
        // Determine player
        if (playerName == null) {
            // Consoles must provide player name
            if (!(sender instanceof Player)) {
                sender.sendMessage(Global.getMessages().get("warp.command.no-player"));
                return;
            }
            player = Global.getPlayer(((Player)sender).getUniqueId());

            if (player == null) {
                sender.sendMessage(Global.getMessages().get("player.unknown", "player", sender.getName()));
                return;
            }

            warpSelf = true;
        } else {
            if (!sender.hasPermission("gesuit.warps.command.warp.other")) {
                sender.sendMessage(Global.getMessages().get("player.no-permission"));
                return;
            }
            
            player = Global.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
                return;
            }
            warpSelf = false;
        }
        
        Warp warp = manager.getWarp(warpName);
        if (warp == null) {
            sender.sendMessage(Global.getMessages().get("warp.unknown-warp", "warp", warpName));
            return;
        }
        
        // Check permission
        if (!sender.hasPermission("gesuit.warps.warp.*") && !sender.hasPermission("gesuit.warps.warp." + warpName.toLowerCase())) {
            if (warp.isHidden()) {
                // Actually hide the warp if they can't use it
                sender.sendMessage(Global.getMessages().get("warp.unknown-warp", "warp", warpName));
            } else {
                sender.sendMessage(Global.getMessages().get("warp.no-permission"));
            }
            return;
        }

        String warpDescription = warp.getDescriptionOrName();

        // Perform the warp
        if (warpSelf) {
            TeleportsModule.getTeleportManager().teleportWithDelay(player, warp.getLocation(), TeleportCause.COMMAND);
            sender.sendMessage(Global.getMessages().get("warp.teleport", "warp", warpDescription));
        } else {        
            TeleportsModule.getTeleportManager().teleport(player, warp.getLocation(), TeleportCause.COMMAND);
            // TODO: Enable sending messages to players
            //player.sendMessage(Global.getMessages().get("warp.teleport", "warp", warpDescription));
            sender.sendMessage(Global.getMessages().get("warp.teleport.other", "player", player.getDisplayName(), "warp", warpDescription));
        }
    }
    
    @CommandTabCompleter(name="warp")
    public Iterable<String> tabCompleteWarp(CommandSender sender, int argument, String input, String playerName, String warpName) {
        if (argument == 0) { // playerName
            if (sender.hasPermission("gesuit.warps.command.warp.other")) {
                return Utilities.matchPlayerNames(input, true);
            } else {
                return null;
            }
        } else {
            input = input.toLowerCase();
            List<String> warps = Lists.newArrayList();
            for (Warp warp : manager.getWarps()) {
                if (sender.hasPermission("gesuit.warps.warp.*") || sender.hasPermission("gesuit.warps.warp." + warp.getName().toLowerCase())) {
                    if (warp.getName().toLowerCase().startsWith(input)) {
                        warps.add(warp.getName());
                    }
                }
            }
            
            return warps;
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name>")
    public void setwarp(Player sender, String warp) {
        boolean isUpdate = manager.hasGlobalWarp(warp);
        manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), false);
        
        if (isUpdate) {
            sender.sendMessage(Global.getMessages().get("warp.update", "warp", warp));
        } else {
            sender.sendMessage(Global.getMessages().get("warp.create", "warp", warp));
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false>")
    public void setwarp(Player sender, String warp, boolean hidden) {
        boolean isUpdate = manager.hasGlobalWarp(warp);
        manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        
        if (isUpdate) {
            sender.sendMessage(Global.getMessages().get("warp.update", "warp", warp));
        } else {
            sender.sendMessage(Global.getMessages().get("warp.create", "warp", warp));
        }
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false> <global true/false>")
    public void setwarp(Player sender, String warp, boolean hidden, boolean global) {
        boolean isUpdate;
        if (global) {
            isUpdate = manager.hasGlobalWarp(warp);
            manager.setGlobalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        } else {
            isUpdate = manager.hasLocalWarp(warp);
            manager.setLocalWarp(warp, LocationUtil.fromBukkit(sender.getLocation(), Global.getServer().getName()), hidden);
        }
        
        if (isUpdate) {
            sender.sendMessage(Global.getMessages().get("warp.update", "warp", warp));
        } else {
            sender.sendMessage(Global.getMessages().get("warp.create", "warp", warp));
        }
    }

    @Command(name="setwarpdesc", async=true, permission="gesuit.warps.command.setwarpdesc", description="Defines a user-friendly description for a warp", usage="/<command> <name> <description>")
    public void setwarpdesc(Player sender, String name, String description) {
        if (manager.hasLocalWarp(name)) {
            manager.setLocalWarpDesc(name, description);
        } else if (manager.hasGlobalWarp(name)) {
            manager.setGlobalWarpDesc(name, description);
        } else {
            sender.sendMessage(Global.getMessages().get("warp.unknown-warp", "warp", name));
            return;
        }

        sender.sendMessage(Global.getMessages().get("warp.update.description", "warp", name));
    }


    @Command(name="delwarp", async=true, aliases={"deletewarp", "removewarp"}, permission="gesuit.warps.command.delwarp", description="Used to delete a specific warp", usage="/<command> <name>")
    public void delwarp(CommandSender sender, String name) {
        if (manager.hasLocalWarp(name)) {
            manager.removeLocalWarp(name);
        } else if (manager.hasGlobalWarp(name)) {
            manager.removeGlobalWarp(name);
        } else {
            sender.sendMessage(Global.getMessages().get("warp.unknown-warp", "warp", name));
            return;
        }
        
        sender.sendMessage(Global.getMessages().get("warp.delete", "warp", name));
    }
    
    @CommandTabCompleter(name="delwarp")
    public Iterable<String> tabCompleteDelWarp(CommandSender sender, int argument, String input, String name) {
        input = input.toLowerCase();
        List<String> warps = Lists.newArrayList();
        for (Warp warp : manager.getWarps()) {
            if (warp.getName().toLowerCase().startsWith(input)) {
                warps.add(warp.getName());
            }
        }
        
        return warps;
    }
}
