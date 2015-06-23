package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.text.SimpleDateFormat;

// TODO: This class needs work
public class PlayerManager {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
    
    public static void sendMessageToTarget(CommandSender target, String message) {
        // Shouldnt need it. But let's be cautious.
        if (target == null) {
            geSuit.getLogger().info("WARNING: sendMessageToTarget(CommandSender, String): Target is null!");
        	return;
        }

        // Not exactly sure where we use the new line besides in the soon-to-be-removed MOTD...
        for (String line : Utilities.colorize(message).split("\n")) {
            if (geSuit.getPlugin().isDebugEnabled()) {
                geSuit.getPlugin().DebugMsg("[SendMessage] " + target.getName() + ": " + Utilities.colorize(line));
    		}
            target.sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
        }
    }

    public static void sendMessageToTarget(String target, String message) {
        if ((target == null) || (target.isEmpty())) {
            geSuit.getLogger().info("WARNING: sendMessageToTarget(String, String): Target is null or empty!");
        	return;
        }
        
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target);
        sendMessageToTarget(player != null ? player : ProxyServer.getInstance().getConsole(), message);
    }

    public static void sendBroadcast(String message) {
    	sendBroadcast(message, null);
    }

    public static void sendBroadcast(String message, String excludedPlayer) {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
        	if ((excludedPlayer != null) && (excludedPlayer.equals(p.getName()))) continue;
        	sendMessageToTarget(p.getName(), message);
        }
        geSuit.getLogger().info(message);
    }
}
