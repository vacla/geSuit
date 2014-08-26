package net.cubespace.geSuit.listeners;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.objects.Ban;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BansListener implements Listener {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    @EventHandler
    public void banCheck(LoginEvent e) throws SQLException {
        if (DatabaseManager.bans.isPlayerBanned(e.getConnection().getName(), e.getConnection().getUUID(), e.getConnection().getAddress().getHostString())) {
            Ban b = DatabaseManager.bans.getBanInfo(e.getConnection().getName(), e.getConnection().getUUID(), e.getConnection().getAddress().getHostString());

            if (b == null) {
                return;
            }

            if (b.getType().equals("tempban")) {
                if (BansManager.checkTempBan(b)) {
                    e.setCancelled(true);

                    Date then = b.getBannedUntil();
                    Date now = new Date();
                    long timeDiff = then.getTime() - now.getTime();
                    long hours = timeDiff / (60 * 60 * 1000);
                    long mins = timeDiff / (60 * 1000) % 60;
                    
                    e.setCancelReason(Utilities.colorize(ConfigManager.messages.TEMP_BAN_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{time}", sdf.format(then)).replace("{left}", Utilities.buildTimeDiffString(timeDiff, 2)).replace("{message}", b.getReason())));
                    LoggingManager.log(ChatColor.RED + e.getConnection().getName() + "'s connection refused due to being banned!");
                }
            } else {
                e.setCancelled(true);

                e.setCancelReason(Utilities.colorize(ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{message}", b.getReason())));
                LoggingManager.log(ChatColor.RED + e.getConnection().getName() + "'s connection refused due to being banned!");
            }
        }
    }
}
