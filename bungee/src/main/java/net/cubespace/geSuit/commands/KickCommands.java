package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.commands.Varargs;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.remote.moderation.BanActions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@SuppressWarnings("deprecation")
public class KickCommands {
    private BanActions actions;
    
    public KickCommands(BanActions actions) {
        this.actions = actions;
    }
    
    @Command(name="!kick", async=true, aliases={"!kickplayer", "!playerkick", "!kickp", "!pkick"}, permission="gesuit.bans.command.kick", usage="/<command> <player> [reason]")
    public void kick(CommandSender sender, String playerName, @Optional @Varargs String reason) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(ChatColor.RED + "Please use the version of this command without the !");
            return;
        }
        
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
    
    @Command(name="!kickall", async=true, aliases={"!kicka"}, permission="gesuit.bans.command.kickall", usage="/<command> [reason]")
    public void kickAll(CommandSender sender, @Optional @Varargs String reason) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(ChatColor.RED + "Please use the version of this command without the !");
            return;
        }
        
        Result result = actions.kickAll(reason);
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
}
