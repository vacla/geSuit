package net.cubespace.geSuit.moderation.commands;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.TrackingActions;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

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
        
        builder.append('"');
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
        
//      ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
//      @Override
//      public void run() {
//          GSPlayer s = PlayerManager.getPlayer(sentBy);
//          final CommandSender sender = (s == null ? ProxyServer.getInstance().getConsole() : s.getProxiedPlayer());
//  
//          List<Track> tracking = null;
//        if (search.contains(".")) {
//            tracking = DatabaseManager.tracking.getPlayerTracking(search, "ip");
//            if (tracking.isEmpty()) { 
//                  PlayerManager.sendMessageToTarget(sender,
//                        ChatColor.RED + "[Tracker] No known accounts match or contain \"" + search + "\"");
//                  return;
//            } else {
//                PlayerManager.sendMessageToTarget(sender,
//                    ChatColor.GREEN + "[Tracker] IP address \"" + search + "\" associated with " + tracking.size() + " accounts:");
//            }
//        } else {
//            String type;
//            String searchString = search;
//            if (searchString.length() > 20) {
//                type = "uuid";
//                searchString = searchString.replace("-", "");
//            } else {
//                type = "name";
//            }
//  

//            
//            tracking = DatabaseManager.tracking.getPlayerTracking(searchString, type);
//            if (tracking.isEmpty()) { 
//                  PlayerManager.sendMessageToTarget(sender,
//                        ChatColor.GREEN + "[Tracker] No known accounts match or contain \"" + searchString + "\"");
//                  return;
//            } else {
//                PlayerManager.sendMessageToTarget(sender,
//                    ChatColor.GREEN + "[Tracker] Player \"" + searchString + "\" associated with " + tracking.size() + " accounts:");
//                
//                if (geSuitPlugin.proxy.getPlayer(searchString) != null) {
//                    final ProxiedPlayer player = geSuitPlugin.proxy.getPlayer(searchString);
//                    geSuitPlugin.proxy.getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
//                        @Override
//                        public void run() {
//                            String location = GeoIPManager.lookup(player.getAddress().getAddress());
//                            if (location != null) {
//                                PlayerManager.sendMessageToTarget(sender, ChatColor.GREEN + "[Tracker] Player " + player.getName() + "'s IP resolves to " + location); 
//                            }
//                        }
//                    });
//                }
//            }
//        }
//  
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        for (Track t : tracking) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(ChatColor.DARK_GREEN);
//            builder.append(" - ");
//            if (t.isNameBanned()) {
//                builder.append(ChatColor.DARK_AQUA);
//                builder.append(t.getPlayer());
//                builder.append(ChatColor.GREEN);
//                if (t.getBanType().equals("ban")) {
//                    builder.append("[Ban]");
//                } else{
//                    builder.append("[Tempban]");
//                }
//            } else {
//                builder.append(t.getPlayer());
//            }
//            
//            builder.append(' ');
//            
//            if (t.isIpBanned()) {
//                builder.append(ChatColor.DARK_AQUA);
//                builder.append(t.getIp());
//                builder.append(ChatColor.GREEN);
//                builder.append("[IPBan]");
//            } else {
//                builder.append(ChatColor.DARK_GREEN);
//                builder.append(t.getIp());
//            }
//            
//            builder.append(ChatColor.GRAY);
//            builder.append(" (");
//            builder.append(sdf.format(t.getLastSeen()));
//            builder.append(')');
//            
//              PlayerManager.sendMessageToTarget(sender, builder.toString());
//          }
//      }
//  });
    }
    
    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <uuid>")
    public void where(CommandSender sender, UUID id) {
        List<Track> tracking = lookup.getHistory(id);
        where0(sender, id, tracking);
    }
    
    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <ip>")
    public void where(CommandSender sender, InetAddress ip) {
        List<Track> tracking = lookup.getHistory(ip);
        where0(sender, ip, tracking);
    }

    @Command(name="where", async=true, permission="gesuit.bans.command.where", usage="/<command> <player>")
    public void where(CommandSender sender, String playerName) {
        GlobalPlayer player = Global.getOfflinePlayer(playerName);
        
        if (player != null) {
            where0(sender, player, lookup.getHistory(player.getName()));
        } else {
            // TODO: Try wildcard match
            throw new UnsupportedOperationException("Not yet implemented");
//          if (!DatabaseManager.players.playerExists(searchString)) {
//          // No exact match... do a partial match
//            PlayerManager.sendMessageToTarget(sender,
//                  ChatColor.AQUA + "[Tracker] No accounts matched exactly \"" + searchString + "\", trying wildcard search..");
//
//            List<String> matches = DatabaseManager.players.matchPlayers(searchString);
//          if (matches.isEmpty()) { 
//              PlayerManager.sendMessageToTarget(sender,
//                      ChatColor.RED + "[Tracker] No known accounts match or contain \"" + searchString + "\"");
//              return;
//          }
//          else if (matches.size() == 1) {
//              if (searchString.length() < 20) {
//                  searchString = matches.get(0);
//              }
//          } else {
//              // Matched too many names, show list of names instead
//              PlayerManager.sendMessageToTarget(sender,
//                      ChatColor.RED + "[Tracker] More than one player matched \"" + searchString + "\":");
//              for (String m : matches) {
//                  PlayerManager.sendMessageToTarget(sender,
//                          ChatColor.AQUA + " - " + m);
//              }
//              return;
//          }
//      }
        }
    }
    
    @Command(name="ontime", async=true, permission="gesuit.bans.command.ontime", usage="/<command> <player>")
    public void ontime(CommandSender sender, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
        
//      final GSPlayer s = PlayerManager.getPlayer(sentBy);
//      final CommandSender sender = (s == null ? ProxyServer.getInstance().getConsole() : s.getProxiedPlayer());
//
//      ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
//          @Override
//          public void run() {
//            BanTarget bt = getBanTarget(player);
//            if ((bt == null) || (bt.gsp == null)) {
//                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
//                return;
//            }
//
//            // Get time records and set online time (if player is online)
//            TimeRecord tr = DatabaseManager.ontime.getPlayerOnTime(bt.uuid);
//            boolean online = (bt.gsp.getProxiedPlayer() != null);
//            if (online) {
//                tr.setTimeSession(System.currentTimeMillis() - bt.gsp.getLoginTime());
//            } else {
//                tr.setTimeSession(-1);
//            }
//            
//            PlayerManager.sendMessageToTarget(sender, ChatColor.DARK_AQUA + "-------- " + ChatColor.YELLOW + bt.dispname + "'s OnTime Statistics" + ChatColor.DARK_AQUA + " --------");
//
//            // Player join date/time + number of days
//            String firstjoin = String.format("%s %s",
//                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(bt.gsp.getFirstOnline()),
//                    DateFormat.getTimeInstance(DateFormat.SHORT).format(bt.gsp.getFirstOnline()));
//              String days = Integer.toString((int) Math.floor((System.currentTimeMillis() - bt.gsp.getFirstOnline().getTime()) / TimeUnit.DAYS.toMillis(1)));
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_FIRST_JOINED
//                    .replace("{date}", firstjoin)
//                    .replace("{days}", days));
//
//            // Current session length 
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_SESSION
//                    .replace("{diff}", (tr.getTimeSession() == -1) ? ChatColor.RED + "Offline" : Utilities.buildTimeDiffString(tr.getTimeSession(), 3)));
//
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_TODAY.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeToday() + tr.getTimeSession(), 3)));
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_WEEK.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeWeek() + tr.getTimeSession(), 3)));
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_MONTH.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeMonth() + tr.getTimeSession(), 3)));
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_YEAR.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeYear() + tr.getTimeSession(), 3)));
//            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_TOTAL.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeTotal() + tr.getTimeSession(), 3)));
//          }
//      });
    }
    
    @Command(name="namehistory", async=true, aliases={"names"}, permission="gesuit.bans.command.namehistory", usage="/<command> <player>")
    public void nameHistory(CommandSender sender, OfflinePlayer player) {
        throw new UnsupportedOperationException("Not yet implemented");
        
//      ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
//      @Override
//      public void run() {
//          GSPlayer s = PlayerManager.getPlayer(sentBy);
//          final CommandSender sender = (s == null ? ProxyServer.getInstance().getConsole() : s.getProxiedPlayer());
//          
//          UUID id;
//          try {
//              id = Utilities.makeUUID(nameOrId);
//          } catch (IllegalArgumentException e) {
//              Map<String, UUID> result = DatabaseManager.players.resolvePlayerNamesHistoric(Arrays.asList(nameOrId));
//              if (result.isEmpty()) {
//                  PlayerManager.sendMessageToTarget(sender,
//                          ChatColor.RED + "Unknown player " + nameOrId);
//                  return;
//              } else {
//                  id = Iterables.getFirst(result.values(), null);
//              }
//          }
//  
//          List<Track> names = DatabaseManager.tracking.getNameHistory(id);
//          
//          PlayerManager.sendMessageToTarget(sender,
//              ChatColor.GREEN + "Player " + nameOrId + " has had " + names.size() + " different names:");
//          
//          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//          for (Track t : names) {
//              StringBuilder builder = new StringBuilder();
//              builder.append(ChatColor.DARK_GREEN);
//              builder.append(" - ");
//              builder.append(t.getPlayer());
//              
//              builder.append(' ');
//              
//              builder.append(ChatColor.GRAY);
//              builder.append(sdf.format(t.getLastSeen()));
//              
//              PlayerManager.sendMessageToTarget(sender, builder.toString());
//          }
//      }
//  });
    }
}
