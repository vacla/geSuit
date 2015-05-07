package net.cubespace.geSuit.moderation.commands;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.commands.Varargs;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.WarnActions;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarnCommands {
    private WarnActions actions;
    
    public WarnCommands(WarnActions actions) {
        this.actions = actions;
    }
    
    @Command(name="warn", aliases={"warnplayer", "dw"}, permission="gesuit.bans.command.warn", usage="/<command> <player> <reason>")
    public void warn(CommandSender sender, String playerName, @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException("Cannot warn unknown player");
        }
        
        Result result = actions.warn(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="warnhistory", aliases={"dst"}, permission="gesuit.bans.command.warnhistory", usage="/<command> <player>")
    public void warnHistory(CommandSender sender, OfflinePlayer player) {
        throw new UnsupportedOperationException("Not yet implemented");
//      ProxyServer.getInstance().getScheduler().runAsync(geSuit.getPlugin(), new Runnable() {
//      @Override
//      public void run() {
//          GSPlayer s = PlayerManager.getPlayer(sentBy);
//          
//          CommandSender sender = (s == null ? ProxyServer.getInstance().getConsole() : s.getProxiedPlayer());
//          
//          // Resolve the target player
//          GSPlayer target = PlayerManager.getPlayer(player);
//          String targetId;
//          if (target == null) {
//              Map<String, UUID> ids = DatabaseManager.players.resolvePlayerNamesHistoric(Arrays.asList(player));
//              UUID id = Iterables.getFirst(ids.values(), null);
//              if (id == null) {
//                  PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED.replace("{player}", player)));
//                  return;
//              }
//              targetId = id.toString().replace("-", "");
//          } else {
//              targetId = target.getUuid();
//          }
//          
//          List<Ban> warns = DatabaseManager.bans.getWarnHistory(player, targetId);
//          if (warns == null || warns.isEmpty()) {
//              PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED.replace("{player}", player)));
//              return;
//          }
//          PlayerManager.sendMessageToTarget(sender, ChatColor.DARK_AQUA + "-------- " + ChatColor.YELLOW + player + "'s Warning History" + ChatColor.DARK_AQUA + " --------");
//          
//          int count = 0;
//          for (Ban b : warns) {
//              SimpleDateFormat sdf = new SimpleDateFormat();
//              sdf.applyPattern("dd MMM yyyy HH:mm");
//  
//              Date now = new Date(); 
//              int age = (int) ((now.getTime() - b.getBannedOn().getTime()) / 1000 / 86400);
//            if (age >= ConfigManager.bans.WarningExpiryDays) {
//                PlayerManager.sendMessageToTarget(sender,
//                        ChatColor.GRAY + "- " +
//                        ChatColor.DARK_GRAY + sdf.format(b.getBannedOn()) +
//                        ChatColor.DARK_GRAY + " (" + ChatColor.DARK_GRAY + b.getBannedBy() + ChatColor.DARK_GRAY + ") " +
//                        ChatColor.DARK_GRAY + b.getReason());
//            } else {
//                count++;
//                PlayerManager.sendMessageToTarget(sender,
//                        ChatColor.YELLOW + String.valueOf(count) + ": " +
//                        ChatColor.GREEN + sdf.format(b.getBannedOn()) +
//                        ChatColor.YELLOW + " (" + ChatColor.GRAY + b.getBannedBy() + ChatColor.YELLOW + ") " +
//                        ChatColor.AQUA + b.getReason());
//            }
//          }
//      }
//  });
    }
}
