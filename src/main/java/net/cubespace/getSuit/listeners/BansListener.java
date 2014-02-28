package net.cubespace.getSuit.listeners;

import net.cubespace.getSuit.managers.BansManager;
import net.cubespace.getSuit.managers.ConfigManager;
import net.cubespace.getSuit.managers.LoggingManager;
import net.cubespace.getSuit.objects.Ban;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BansListener implements Listener {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    @EventHandler
    public void banCheck(LoginEvent e) throws SQLException {
        if (BansManager.isPlayerBanned(e.getConnection().getName())) {
            Ban b = BansManager.getBanInfo(e.getConnection().getName());
            if (b.getType().equals("tempban")) {
                if (BansManager.checkTempBan(b)) {
                    e.setCancelled(true);

                    Date then = b.getBannedUntil();
                    Date now = new Date();
                    long timeDiff = then.getTime() - now.getTime();
                    long hours = timeDiff / (60 * 60 * 1000);
                    long mins = timeDiff / (60 * 1000) % 60;

                    e.setCancelReason(ConfigManager.messages.TEMP_BAN_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{time}", sdf.format(then) + " (" + hours + ":" + mins + " hours)").replace("{message}", b.getReasaon()));
                    LoggingManager.log(ChatColor.RED + e.getConnection().getName() + "'s connection refused due to being banned!");
                }
            } else {
                e.setCancelled(true);

                e.setCancelReason(ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{message}", b.getReasaon()));
                LoggingManager.log(ChatColor.RED + e.getConnection().getName() + "'s connection refused due to being banned!");
            }
        }
    }
}
