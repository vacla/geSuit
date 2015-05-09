package net.cubespace.geSuit.moderation.commands;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.commands.Varargs;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.remote.moderation.BanActions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class KickCommands {
    private BanActions actions;
    
    public KickCommands(BanActions actions) {
        this.actions = actions;
    }
    
    @Command(name="kick", async=true, aliases={"kickplayer", "playerkick", "kickp", "pkick"}, permission="gesuit.bans.command.kick", usage="/<command> <player> [reason]")
    public void kick(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player is not online");
            return;
        }
        
        Result result = actions.kick(player, reason);
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="kickall", async=true, aliases={"kicka"}, permission="gesuit.bans.command.kickall", usage="/<command> [reason]")
    public void kickAll(CommandSender sender, @Optional @Varargs String reason) {
        Result result = actions.kickAll(reason);
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
}
