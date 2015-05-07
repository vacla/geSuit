package net.cubespace.geSuit.moderation.commands;

import java.net.InetAddress;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.commands.Optional;
import net.cubespace.geSuit.commands.Varargs;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.BanActions;

public class BanCommands {
    private BanActions actions;
    
    public BanCommands(BanActions actions) {
        this.actions = actions;
    }
    
    @Command(name="ban", async=true, aliases={"db","banplayer"}, permission="gesuit.bans.command.ban", usage="/<command> <player> [reason]")
    public void ban(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot ban unknown player " + playerName);
        }
        
        Result result = actions.ban(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipban", aliases={"dbip","banip"}, permission="gesuit.bans.command.ipban", usage="/<command> <ip> [reason]")
    public void ipBan(CommandSender sender, InetAddress ip, @Optional @Varargs String reason) {
        Result result = actions.ban(ip, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipban", aliases={"dbip","banip"}, permission="gesuit.bans.command.ipban", usage="/<command> <player> [reason]")
    public void ipBan(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot ipban unknown player " + playerName);
        }
        
        Result result = actions.ipban(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tempbanip", aliases={"tbanip","dtbip"}, permission="gesuit.bans.command.tempbanip", usage="/<command> <ip> <time> [reason]")
    public void tempBan(CommandSender sender, InetAddress ip, DateDiff date, @Optional @Varargs String reason) {
        Result result = actions.banUntil(ip, reason, date.fromNow(), sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tempban", aliases={"tban","bant","bantemp","dtb"}, permission="gesuit.bans.command.tempban", usage="/<command> <player> <time> [reason]")
    public void tempBan(CommandSender sender, String playerName, DateDiff date, @Optional @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot tempban unknown player " + playerName);
        }
        
        Result result = actions.banUntil(player, reason, date.fromNow(), sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="unban", aliases={"dub","uban", "reoveban", "pardon"}, permission="gesuit.bans.command.unban", usage="/<command> <player> [reason]")
    public void unban(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot unban unknown player " + playerName);
        }
        
        Result result = actions.unban(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="unbanip", aliases={"ipunban","unipban", "ipsafe", "safeip", "pardonip", "dubip"}, permission="gesuit.bans.command.ipban", usage="/<command> <player> [reason]")
    public void unbanIp(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot unban unknown player " + playerName);
        }
        
        Result result = actions.ipunban(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="unbanip", aliases={"ipunban","unipban", "ipsafe", "safeip", "pardonip", "dubip"}, permission="gesuit.bans.command.ipban", usage="/<command> <player> [reason]")
    public void unbanIp(CommandSender sender, InetAddress ip, @Optional @Varargs String reason) {
        Result result = actions.unban(ip, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="banhistory", permission="gesuit.bans.command.banhistory", usage="/<command> <player>")
    public void banHistory(CommandSender sender, OfflinePlayer player) {
        throw new UnsupportedOperationException("Not yet implemented");
        
//      BanTarget t = getBanTarget(player);
//      List<Ban> bans = DatabaseManager.bans.getBanHistory(t.name);
//
//      if (bans == null || bans.isEmpty()) {
//          PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_BANNED.replace("{player}", t.dispname)));
//          return;
//      }
//      PlayerManager.sendMessageToTarget(sender, ChatColor.DARK_AQUA + "-------- " + ChatColor.YELLOW + player + "'s Ban History" + ChatColor.DARK_AQUA + " --------");
//      boolean first = true;
//      for (Ban b : bans) {
//          if (first) {
//              first = false;
//          } else {
//              PlayerManager.sendMessageToTarget(sender, "");
//          }
//          SimpleDateFormat sdf = new SimpleDateFormat();
//          sdf.applyPattern("dd MMM yyyy HH:mm");
//
//          PlayerManager.sendMessageToTarget(sender,
//                (b.getBannedUntil() != null ? ChatColor.GOLD : ChatColor.RED) + "Date: " +
//                ChatColor.GREEN + sdf.format(b.getBannedOn()) +
//                (b.getBannedUntil() != null ?
//                        ChatColor.GOLD + " > " + sdf.format(b.getBannedUntil()) :
//                        ChatColor.RED + " > permban"));
//
//          PlayerManager.sendMessageToTarget(sender,
//                (b.getBannedUntil() != null ? ChatColor.GOLD : ChatColor.RED) + "By: " +
//                ChatColor.AQUA + b.getBannedBy() +
//                ChatColor.DARK_AQUA + " (" + ChatColor.GRAY + b.getReason() + ChatColor.DARK_AQUA + ")");
//      }
    }
    
    @Command(name="checkban", aliases={"lookupban","baninfo"}, permission="gesuit.bans.command.checkban", usage="/<command> <player>")
    public void checkBan(CommandSender sender, String playerName) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot unban unknown player " + playerName);
        }
        
        throw new UnsupportedOperationException("Not yet implemented");
//      BanTarget t = getBanTarget(player);
//      Ban b = DatabaseManager.bans.getBanInfo(t.name);
//
//      if (b == null) {
//          PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_NOT_BANNED);
//      } else {
//          SimpleDateFormat sdf = new SimpleDateFormat();
//          sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
//          PlayerManager.sendMessageToTarget(sender, ChatColor.DARK_AQUA + "-------- " + ChatColor.RED + "Ban Info" + ChatColor.DARK_AQUA + " --------");
//          PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Player: " + ChatColor.AQUA + b.getPlayer());
//          if (b.getUuid() != null) {
//              PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "UUID: " + ChatColor.AQUA + b.getUuid());
//          }
//          PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Type: " + ChatColor.AQUA + b.getType());
//          PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "By: " + ChatColor.AQUA + b.getBannedBy());
//          PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Reason: " + ChatColor.AQUA + b.getReason());
//          PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Date: " + ChatColor.AQUA + sdf.format(b.getBannedOn()));
//
//          if (b.getBannedUntil() == null) {
//              PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Until: " + ChatColor.AQUA + "-Forever-");
//          } else {
//              PlayerManager.sendMessageToTarget(sender, ChatColor.RED + "Until: " + ChatColor.AQUA + sdf.format(b.getBannedUntil()));
//          }
//      }
    }
}
