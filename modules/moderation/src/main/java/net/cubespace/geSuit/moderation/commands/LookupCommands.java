package net.cubespace.geSuit.moderation.commands;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandPriority;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.TimeRecord;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.TrackingActions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

public class LookupCommands {
    private TrackingActions lookup;
    
    public LookupCommands(TrackingActions lookup) {
        this.lookup = lookup;
    }
    
    private void where0(CommandSender sender, Object who, List<Track> tracking) {
        String whoString;
        
        if (who instanceof GlobalPlayer) {
            whoString = ((GlobalPlayer)who).getDisplayName();
        } else if (who instanceof InetAddress) {
            whoString = ((InetAddress)who).getHostAddress();
        } else {
            whoString = who.toString();
        }
        
        if (tracking.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "[Tracker] No known accounts match or contain \"" + whoString + "\"");
            return;
        }
        
        // Header
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GREEN);
        builder.append("[Tracker] ");
        
        if (who instanceof GlobalPlayer) {
            builder.append("Player");
        } else if (who instanceof InetAddress) {
            builder.append("IP address");
        } else {
            builder.append("UUID");
        }
        
        builder.append(" \"");
        builder.append(whoString);
        builder.append("\" associated with ");
        builder.append(tracking.size());
        builder.append(" accounts:");
        
        sender.sendMessage(builder.toString());
        
        // Contents
        for (Track track : tracking) {
            builder = new StringBuilder();
            builder.append(ChatColor.DARK_GREEN);
            builder.append(" - ");
            if (track.isNameBanned()) {
                builder.append(ChatColor.DARK_AQUA);
            }

            builder.append(track.getName());
            if (track.getNickname() != null) {
                builder.append(" (");
                builder.append(track.getNickname());
                builder.append(") ");
            }

            if (track.isNameBanned()) {
                builder.append(ChatColor.GREEN);
                if (track.isNameBanTemp()) {
                    builder.append("[Tempban]");
                } else {
                    builder.append("[Ban]");
                }
            }

            builder.append(' ');

            if (track.isIpBanned()) {
                builder.append(ChatColor.DARK_AQUA);
                builder.append(track.getIp().getHostAddress());
                builder.append(ChatColor.GREEN);
                if (track.isIpBanTemp()) {
                    builder.append("[Temp IPBan]");
                } else {
                    builder.append("[IPBan]");
                }
            } else {
                builder.append(ChatColor.DARK_GREEN);
                builder.append(track.getIp().getHostAddress());
            }

            builder.append(ChatColor.GRAY);
            builder.append(" (");
            builder.append(Utilities.formatDate(track.getLastSeen()));
            builder.append(')');

            sender.sendMessage(builder.toString());
        }
    }
    
    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <uuid>")
    @CommandPriority(3)
    public void where(CommandSender sender, UUID id) {
        List<Track> tracking = lookup.getHistory(id);
        where0(sender, id, tracking);
    }
    
    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <ip>")
    @CommandPriority(2)
    public void where(CommandSender sender, InetAddress ip) {
        List<Track> tracking = lookup.getHistory(ip);
        where0(sender, ip, tracking);
    }

    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <player>")
    @CommandPriority(1)
    public void where(CommandSender sender, String playerName) {
        GlobalPlayer player = Global.getOfflinePlayer(playerName);
        
        if (player != null) {
            where0(sender, player, lookup.getHistory(player.getName()));
        } else {
            sender.sendMessage(ChatColor.AQUA + "[Tracker] No accounts matched exactly \"" + playerName + "\", trying wildcard search..");
            List<UUID> results = lookup.matchPlayers(playerName);
            
            if (results.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "[Tracker] No known accounts match or contain \"" + playerName + "\"");
                return;
            }
            
            // Just do the where on it
            if (results.size() == 1) {
                player = Global.getOfflinePlayer(results.get(0));
                where0(sender, player, lookup.getHistory(player.getUniqueId()));
                return;
            }
            
            // Display possibles
            // Build name list, may be slow?
            List<String> names = Lists.newArrayListWithCapacity(results.size());
            for (UUID id : results) {
                GlobalPlayer target = Global.getOfflinePlayer(id);
                names.add(target.getDisplayName());
            }
            
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
            // Display the list
            sender.sendMessage(ChatColor.RED + "[Tracker] More than one player matched \"" + playerName + "\":");
            for (String name : names) {
                sender.sendMessage(ChatColor.AQUA + " - " + name);
            }
        }
    }
    
    @Command(name = "ontime", async = true, permission = "gesuit.bans.command.ontime", usage = "/<command> <player>")
    public void ontime(CommandSender sender, String playerName) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);

        if (player == null) {
            throw new IllegalArgumentException(Global.getMessages().get("ban.unknown-player", "player", playerName));
        }

        TimeRecord record = lookup.getOntime(player);
        boolean online = (Global.getPlayer(player.getUniqueId()) != null);

        if (online) {
            record.setTimeSession(System.currentTimeMillis() - player.getSessionJoin());
        } else {
            record.setTimeSession(-1);
        }
        
        sender.sendMessage(Global.getMessages().get("ontime.header", "player", player.getDisplayName()));

        // Player join date/time + number of days
        String firstJoin = String.format("%s %s", DateFormat.getDateInstance(DateFormat.MEDIUM).format(player.getFirstJoined()), DateFormat.getTimeInstance(DateFormat.SHORT).format(player.getFirstJoined()));
        String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - player.getFirstJoined()));
        sender.sendMessage(Global.getMessages().get("ontime.first-joined", "date", firstJoin, "days", days));

        // Current session length
        if (online) {
            sender.sendMessage(Global.getMessages().get("ontime.time-session", "diff", new DateDiff(record.getTimeSession()).toLongString(3)));
        }
        
        sender.sendMessage(Global.getMessages().get("ontime.time-today", "diff", new DateDiff(record.getTimeToday() + record.getTimeSession()).toLongString(3)));
        sender.sendMessage(Global.getMessages().get("ontime.time-week", "diff", new DateDiff(record.getTimeWeek() + record.getTimeSession()).toLongString(3)));
        sender.sendMessage(Global.getMessages().get("ontime.time-month", "diff", new DateDiff(record.getTimeMonth() + record.getTimeSession()).toLongString(3)));
        sender.sendMessage(Global.getMessages().get("ontime.time-year", "diff", new DateDiff(record.getTimeYear() + record.getTimeSession()).toLongString(3)));
        sender.sendMessage(Global.getMessages().get("ontime.time-total", "diff", new DateDiff(record.getTimeTotal() + record.getTimeSession()).toLongString(3)));
    }
    
    @Command(name="namehistory", async=true, aliases={"names"}, permission="gesuit.bans.command.namehistory", usage="/<command> <player>")
    @CommandPriority(1)
    public void nameHistory(CommandSender sender, UUID id) {
        GlobalPlayer player = Global.getOfflinePlayer(id);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", id.toString()));
            return;
        }
        
        nameHistory0(sender, player, id);
    }
    
    @Command(name="namehistory", async=true, aliases={"names"}, permission="gesuit.bans.command.namehistory", usage="/<command> <player>")
    @CommandPriority(2)
    public void nameHistory(CommandSender sender, String playerName) {
        GlobalPlayer player = Global.getOfflinePlayer(playerName);
        
        if (player == null) {
            List<UUID> owners = lookup.matchFullPlayers(playerName);
            
            if (owners.isEmpty()) {
                sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
                return;
            }
            
            if (owners.size() == 1) {
                nameHistory0(sender, Global.getOfflinePlayer(owners.get(0)), playerName);
            } else {
                // Display ambiguity thing
                List<String> names = Lists.newArrayListWithCapacity(owners.size());
                for (UUID id : owners) {
                    GlobalPlayer target = Global.getOfflinePlayer(id);
                    names.add(target.getDisplayName());
                }
                
                Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
                
                // Display the list
                sender.sendMessage(ChatColor.GOLD + playerName + " is ambiguous and can refer to the following players:");
                for (String name : names) {
                    sender.sendMessage(ChatColor.AQUA + " - " + name);
                }
            }
        } else {
            nameHistory0(sender, player, playerName);
        }
    }
    
    private void nameHistory0(CommandSender sender, GlobalPlayer player, Object input) {
        List<Track> names = lookup.getNameHistory(player);
        sender.sendMessage(ChatColor.GREEN + "Player " + input + " has had " + names.size() + " different names:");

        for (Track t : names) {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.DARK_GREEN);
            builder.append(" - ");
            builder.append(t.getDisplayName());

            builder.append(' ');

            builder.append(ChatColor.GRAY);
            builder.append(Utilities.formatDate(t.getLastSeen()));

            sender.sendMessage(builder.toString());
        }
    }
}
