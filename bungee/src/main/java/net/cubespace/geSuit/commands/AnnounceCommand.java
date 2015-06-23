package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Command;

public class AnnounceCommand extends Command {
    private long lastBroadcastTime;
    private String lastBroadcastUser;
    private String lastBroadcastName;
    
    private BroadcastManager manager;
    public AnnounceCommand(BroadcastManager manager) {
        super("!announce");
        
        this.manager = manager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.announce") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, Global.getMessages().get("player.no-permission"));
            return;
        }
        
        // Show last broadcast done
        if (args.length == 0) {
            showLastBroadcast(sender);
            return;
        }
        
        if (args.length > 2) {
            sender.sendMessage("Usage: /!announce [<name> [test]]");
            return;
        }
        
        // Test mode?
        boolean isTest = false;
        if (args.length == 2) {
            if (!args[1].equalsIgnoreCase("test")) {
                sender.sendMessage("Usage: /!announce [<name> [test]]");
                return;
            }
            isTest = true;
        }
        
        // Get the broadcast
        BaseComponent[] message = manager.getDefinedBroadcast(args[0]);
        if (message == null) {
            sender.sendMessage(Global.getMessages().get("broadcast.unknown", "name", args[0]));
            return;
        }
        
        // Can we broadcast?
        if (!isTest) {
            long nextBroadcastTime = lastBroadcastTime + manager.getManualBroadcastCooldown();
            
            if (System.currentTimeMillis() < nextBroadcastTime) {
                sender.sendMessage(Global.getMessages().get("broadcast.cooldown"));
                showLastBroadcast(sender);
                return;
            }
        }
        
        // Broadcast
        if (isTest) {
            sender.sendMessage(Global.getMessages().get("broadcast.test"));
            sender.sendMessage(message);
        } else {
            manager.broadcastGlobal(message);
            
            // Update last information
            lastBroadcastTime = System.currentTimeMillis();
            lastBroadcastName = args[0].toLowerCase();
            lastBroadcastUser = sender.getName();
        }
    }
    
    private void showLastBroadcast(CommandSender sender) {
        if (lastBroadcastTime == 0) {
            sender.sendMessage(Global.getMessages().get("broadcast.last.not-used"));
        } else {
            String time = Utilities.formatDate(lastBroadcastTime);
            DateDiff diff = new DateDiff(System.currentTimeMillis() - lastBroadcastTime);
            
            sender.sendMessage(Global.getMessages().get(
                    "broadcast.last",
                    "name", lastBroadcastName,
                    "by", lastBroadcastUser,
                    "time", time,
                    "diff", diff.toLongString(2),
                    "shortdiff", diff.toString(2)
                    ));
        }
    }
}


