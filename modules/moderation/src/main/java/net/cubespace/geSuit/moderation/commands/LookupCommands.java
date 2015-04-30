package net.cubespace.geSuit.moderation.commands;

import java.net.InetAddress;
import java.util.UUID;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.remote.moderation.TrackingActions;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class LookupCommands {
    private TrackingActions lookup;
    
    public LookupCommands(TrackingActions lookup) {
        this.lookup = lookup;
    }
    
    @Command(name="where", permission="gesuit.bans.command.where", usage="/<command> <uuid>")
    public void where(CommandSender sender, UUID id) {
        throw new UnsupportedOperationException("Not yet implemented");
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
//            if (!DatabaseManager.players.playerExists(searchString)) {
//                // No exact match... do a partial match
//                  PlayerManager.sendMessageToTarget(sender,
//                        ChatColor.AQUA + "[Tracker] No accounts matched exactly \"" + searchString + "\", trying wildcard search..");
//  
//                  List<String> matches = DatabaseManager.players.matchPlayers(searchString);
//                if (matches.isEmpty()) { 
//                    PlayerManager.sendMessageToTarget(sender,
//                            ChatColor.RED + "[Tracker] No known accounts match or contain \"" + searchString + "\"");
//                    return;
//                }
//                else if (matches.size() == 1) {
//                    if (searchString.length() < 20) {
//                        searchString = matches.get(0);
//                    }
//                } else {
//                    // Matched too many names, show list of names instead
//                    PlayerManager.sendMessageToTarget(sender,
//                            ChatColor.RED + "[Tracker] More than one player matched \"" + searchString + "\":");
//                    for (String m : matches) {
//                        PlayerManager.sendMessageToTarget(sender,
//                                ChatColor.AQUA + " - " + m);
//                    }
//                    return;
//                }
//            }
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
    
    @Command(name="where", permission="gesuit.bans.command.where", usage="/<command> <ip>")
    public void where(CommandSender sender, InetAddress ip) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Command(name="where", permission="gesuit.bans.command.where", usage="/<command> <player>")
    public void where(CommandSender sender, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="ontime", permission="gesuit.bans.command.ontime", usage="/<command> <player>")
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
    
    @Command(name="namehistory", aliases={"names"}, permission="gesuit.bans.command.namehistory", usage="/<command> <player>")
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
