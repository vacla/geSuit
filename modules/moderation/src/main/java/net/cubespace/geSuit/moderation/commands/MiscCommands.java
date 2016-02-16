package net.cubespace.geSuit.moderation.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.util.Utilities;

public class MiscCommands {
    @Command(name="nickname", async=true, aliases={"nick"}, permission="gesuit.bans.command.nick.other", usage="/<command> <player> <name>|off")
    public void nickname(CommandSender sender, String playerName, String newName) {
        GlobalPlayer player = Utilities.getPlayerAdvanced(playerName);
        
        if (player == null) {
            throw new IllegalArgumentException(Global.getMessages().get("player.unknown", "player", playerName));
        }
        
        try {
            if (newName.equals("off")) {
                player.setNickname(null);
                sender.sendMessage(Global.getMessages().get("nick.removed", "player", player.getName()));
            } else {
                player.setNickname(newName);
                sender.sendMessage(Global.getMessages().get("nick.added", "player", player.getName(), "name", newName));
            }
            
            player.saveIfModified();
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Global.getMessages().get("nick.duplicate", "player", player.getName()));
        }
    }
    
    @Command(name="nickname", async=true, aliases={"nick"}, permission="gesuit.bans.command.nick", usage="/<command> <name>|off")
    public void nickname(CommandSender sender, String newName) {
        if (!(sender instanceof Player)) {
            throw new IllegalArgumentException(Global.getMessages().get("player.required"));
        }
        
        GlobalPlayer player = Global.getPlayer(((Player)sender).getUniqueId());
        
        try {
            if (newName.equals("off")) {
                player.setNickname(null);
                sender.sendMessage(Global.getMessages().get("nick.removed", "player", player.getName()));
            } else {
                player.setNickname(newName);
                sender.sendMessage(Global.getMessages().get("nick.added", "player", player.getName(), "name", newName));
            }
            
            player.saveIfModified();
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Global.getMessages().get("nick.duplicate", "player", player.getName()));
        }
    }
}
