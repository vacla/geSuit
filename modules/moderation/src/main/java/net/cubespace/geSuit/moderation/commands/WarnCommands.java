package net.cubespace.geSuit.moderation.commands;

import java.util.List;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Varargs;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.WarnInfo;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.WarnActions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarnCommands {
    private WarnActions actions;
    
    public WarnCommands(WarnActions actions) {
        this.actions = actions;
    }
    
    @Command(name="warn", async=true, aliases={"warnplayer", "dw"}, permission="gesuit.bans.command.warn", usage="/<command> <player> <reason>")
    public void warn(CommandSender sender, String playerName, @Varargs String reason) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException(Global.getMessages().get("warn.unknown-player"));
        }
        
        Result result = actions.warn(player, reason, sender.getName(), (sender instanceof Player ? ((Player)sender).getUniqueId() : null));
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }

    @Command(name = "warnhistory", async = true, aliases = {"dst"}, permission = "gesuit.bans.command.warnhistory.other", usage = "/<command> <player>")
    public void warnHistory(CommandSender sender, String playerName) {
        // TODO: Somehow allow lookup by previous name
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);

        if (player == null) {
            throw new IllegalArgumentException(Global.getMessages().get("player.unknown", "player", playerName));
        }

        List<WarnInfo> warnings = actions.getWarnings(player);

        sender.sendMessage(ChatColor.DARK_AQUA + "-------- " + ChatColor.YELLOW + player.getDisplayName() + "'s Warning History" + ChatColor.DARK_AQUA + " --------");
        if (warnings.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "That player has no warnings");
        }

        int count = 0;
        for (WarnInfo warn : warnings) {
            if (System.currentTimeMillis() > warn.getExpireDate()) {
                sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.DARK_GRAY + Utilities.formatDate(warn.getDate()) + " (" + warn.getBy() + ") " + warn.getReason());
            } else {
                ++count;
                sender.sendMessage(ChatColor.YELLOW + String.valueOf(count) + ": " +
                        ChatColor.GREEN + Utilities.formatDate(warn.getDate()) +
                        ChatColor.YELLOW + " (" + ChatColor.GRAY + warn.getBy() + ChatColor.YELLOW + ") " +
                        ChatColor.AQUA + warn.getReason());
            }
        }
    }
    
    @Command(name="warnhistory", async=true, aliases={"dst"}, permission="gesuit.bans.command.warnhistory", usage="/<command> <player>")
    public void warnHistory(CommandSender sender) {
        // TODO: Somehow allow lookup by previous name
        String playerName = sender.getName();
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException(Global.getMessages().get("player.unknown", "player", playerName));
        }
        
        List<WarnInfo> warnings = actions.getWarnings(player);
        
        sender.sendMessage(ChatColor.DARK_AQUA + "-------- " + ChatColor.YELLOW + player.getDisplayName() + "'s Warning History" + ChatColor.DARK_AQUA + " --------");
        if (warnings.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "That player has no warnings");
        }
        
        int count = 0;
        for (WarnInfo warn : warnings) {
            String warnby = "Unknown";
            if (sender.hasPermission("gesuit.bans.command.warnhistory.seewarnby")) {
                warnby = warn.getBy();
            }

            if (System.currentTimeMillis() > warn.getExpireDate()) {
                sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.DARK_GRAY + Utilities.formatDate(warn.getDate()) + " (" + warnby + ") " + warn.getReason());
            } else {
                ++count;
                sender.sendMessage(ChatColor.YELLOW + String.valueOf(count) + ": " +
                    ChatColor.GREEN + Utilities.formatDate(warn.getDate()) +
                        ChatColor.YELLOW + " (" + ChatColor.GRAY + warnby + ChatColor.YELLOW + ") " +
                    ChatColor.AQUA + warn.getReason());
            }
        }
    }
}
