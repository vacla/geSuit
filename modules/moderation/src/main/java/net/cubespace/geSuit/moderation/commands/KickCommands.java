package net.cubespace.geSuit.moderation.commands;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.commands.Optional;
import net.cubespace.geSuit.commands.Varargs;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.remote.moderation.BanActions;

import org.bukkit.command.CommandSender;

public class KickCommands {
    private BanActions actions;
    
    public KickCommands(BanActions actions) {
        this.actions = actions;
    }
    
    @Command(name="kick", aliases={"kickplayer", "playerkick", "kickp", "pkick"}, permission="gesuit.bans.command.kick", usage="/<command> <player> [reason]")
    public void kick(CommandSender sender, GlobalPlayer player, @Optional @Varargs String reason) {
        Result result = actions.kick(player, reason);
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="kickall", aliases={"kicka"}, permission="gesuit.bans.command.kickall", usage="/<command> [reason]")
    public void kickAll(CommandSender sender, @Optional @Varargs String reason) {
        Result result = actions.kickAll(reason);
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
}
