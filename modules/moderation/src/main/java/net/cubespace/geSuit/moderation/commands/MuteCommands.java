package net.cubespace.geSuit.moderation.commands;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandPriority;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Tuple;
import net.cubespace.geSuit.remote.moderation.MuteActions;

public class MuteCommands {
    private final MuteActions actions;
    
    public MuteCommands(MuteActions actions) {
        this.actions = actions;
    }
    
    @Command(name="mute", async=true, permission="gesuit.mutes.command.mute", usage="/<command> <player> [<time>]")
    public void mute(CommandSender sender, String playerName, @Optional DateDiff length) {
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.not-online"));
            return;
        }
        
        Result result;
        
        if (length == null) {
            result = actions.mute(player, sender.getName());
        } else {
            result = actions.mute(player, length.fromNow(), sender.getName());
        }
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipmute", aliases={"muteip"}, async=true, permission="gesuit.mutes.command.ipmute", usage="/<command> <ip> [<time>]")
    @CommandPriority(2)
    public void ipmute(CommandSender sender, InetAddress ip, @Optional DateDiff length) {
        Result result;
        
        if (length == null) {
            result = actions.mute(ip, sender.getName());
        } else {
            result = actions.mute(ip, length.fromNow(), sender.getName());
        }
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipmute", aliases={"muteip"}, async=true, permission="gesuit.mutes.command.ipmute", usage="/<command> <player> [<time>]")
    @CommandPriority(1)
    public void ipmute(CommandSender sender, String playerName, @Optional DateDiff length) {
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.not-online"));
            return;
        }
        
        Result result;
        
        if (length == null) {
            result = actions.muteIp(player, sender.getName());
        } else {
            result = actions.muteIp(player, length.fromNow(), sender.getName());
        }
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="unmute", async=true, permission="gesuit.mutes.command.unmute", usage="/<command> <player>")
    public void unmute(CommandSender sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.not-online"));
            return;
        }
        
        Result result = actions.unmute(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipunmute", async=true, permission="gesuit.mutes.command.ipunmute", usage="/<command> <ip>")
    @CommandPriority(2)
    public void ipunmute(CommandSender sender, InetAddress ip) {
        Result result = actions.unmute(ip);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="ipunmute", async=true, permission="gesuit.mutes.command.ipunmute", usage="/<command> <player>")
    @CommandPriority(1)
    public void ipunmute(CommandSender sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.not-online"));
            return;
        }
        
        Result result = actions.unmuteIp(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="globalmute", aliases={"gmute"}, async=true, permission="gesuit.mutes.command.globalmute", usage="/<command> [<time>]")
    public void globalmute(CommandSender sender, @Optional DateDiff length) {
        Result result;
        
        if (length == null) {
            result = actions.enableGlobalMute(sender.getName());
        } else {
            result = actions.enableGlobalMute(length.fromNow(), sender.getName());
        }
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="globalunmute", aliases={"gunmute"}, async=true, permission="gesuit.mutes.command.globalmute", usage="/<command>")
    public void globalunmute(CommandSender sender) {
        Result result = actions.disableGlobalMute();
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    private String getTimeString(long expire) {
        if (expire == MuteActions.Permanent) {
            return Global.getMessages().get("mute.list.permanent");
        } else {
            return new DateDiff(expire - System.currentTimeMillis()).toString(2);
        }
    }
    
    @Command(name="mutelist", async=true, permission="gesuit.mutes.command.mutelist", usage="/<command>")
    public void muteList(CommandSender sender) {
        Map<UUID, Long> mutedPlayers = actions.getMutedPlayers();
        Map<Tuple<InetAddress, String>, Long> mutedIps = actions.getMutedIPs();
        Tuple<Boolean, Long> globalMute = actions.getGlobalMute();
        
        List<String> entries = Lists.newArrayList();
        if (globalMute.getA()) {
            entries.add(Global.getMessages().get("mute.list.info.global", "time", getTimeString(globalMute.getB())));
        }
        
        // Add all muted players
        for (Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            // Check that it is still valid
            if (entry.getValue() == MuteActions.Permanent || System.currentTimeMillis() < entry.getValue()) {
                // Format and add it
                GlobalPlayer player = Global.getOfflinePlayer(entry.getKey());
                entries.add(Global.getMessages().get("mute.list.info.player", "player", player.getDisplayName(), "time", getTimeString(entry.getValue())));
            }
        }
        
        // Add all ipmuted players/addresses
        for (Entry<Tuple<InetAddress, String>, Long> entry : mutedIps.entrySet()) {
            // Check that it is still valid
            if (entry.getValue() == MuteActions.Permanent || System.currentTimeMillis() < entry.getValue()) {
                // Format and add it
                entries.add(Global.getMessages().get("mute.list.info.group", "player", entry.getKey().getB(), "time", getTimeString(entry.getValue())));
            }
        }
        
        if (entries.isEmpty()) {
            sender.sendMessage(Global.getMessages().get("mute.list.empty"));
        } else {
            sender.sendMessage(Global.getMessages().get("mute.list.header"));
            sender.sendMessage(Global.getMessages().get("mute.list.color") + StringUtils.join(entries, Global.getMessages().get("mute.list.color") + ", "));
        }
    }
}
