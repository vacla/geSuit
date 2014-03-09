package net.cubespace.geSuit.managers;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.objects.Ban;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BansManager {
    public static void banPlayer(String bannedBy, String player, String reason) {
        GSPlayer p = PlayerManager.getPlayer(bannedBy);
        GSPlayer t = PlayerManager.getSimilarPlayer(player);

        if (t == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }

        if (DatabaseManager.bans.isPlayerBanned(t.getName()) || DatabaseManager.bans.isPlayerBanned(DatabaseManager.players.getPlayerIP(t.getName()))) {
            p.sendMessage(ConfigManager.messages.PLAYER_ALREADY_BANNED);
            return;
        }

        if (reason.equals("")) {
            reason = ConfigManager.messages.DEFAULT_BAN_REASON;
        }

        DatabaseManager.bans.banPlayer(player, bannedBy, (t.getUuid() != null) ? t.getUuid() : t.getName(), reason, "ban");

        if (t.getProxiedPlayer() != null) {
            disconnectPlayer(t.getProxiedPlayer(), ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{message}", reason).replace("{sender}", bannedBy));
        }

        if (ConfigManager.bans.BroadcastBans) {
            PlayerManager.sendBroadcast(ConfigManager.messages.BAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        } else {
            p.sendMessage(ConfigManager.messages.BAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        }
    }

    public static void unbanPlayer(String sender, String player) {
        if (!Utilities.isIPAddress(player) && !DatabaseManager.players.playerExists(player)) {
            PlayerManager.sendMessageToPlayer(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }

        if (!DatabaseManager.bans.isPlayerBanned(player) && !DatabaseManager.bans.isPlayerBanned(DatabaseManager.players.getPlayerIP(player))) {
            PlayerManager.sendMessageToPlayer(sender, ConfigManager.messages.PLAYER_NOT_BANNED);
            return;
        }

        GSPlayer p = DatabaseManager.players.loadPlayer(player);
        Ban b = DatabaseManager.bans.getBanInfo((FeatureDetector.canUseUUID()) ? p.getUuid() : p.getName());

        if (b == null) {
            b = DatabaseManager.bans.getBanInfo(DatabaseManager.players.getPlayerIP(player));
        }

        DatabaseManager.bans.unbanPlayer(b.getId());

        if (ConfigManager.bans.BroadcastBans) {
            PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_UNBANNED.replace("{player}", player).replace("{sender}", sender));
        } else {
            PlayerManager.sendMessageToPlayer(sender, ConfigManager.messages.PLAYER_UNBANNED.replace("{player}", player).replace("{sender}", sender));
        }
    }

    public static void banIP(String bannedBy, String player, String reason) {
        if (reason.equals("")) {
            reason = ConfigManager.messages.DEFAULT_BAN_REASON;
        }

        String ip;
        if (Utilities.isIPAddress(player)) {
            ip = player;
        } else {
            GSPlayer t = PlayerManager.getSimilarPlayer(player);
            ip = DatabaseManager.players.getPlayerIP(t.getName());
        }

        if (!DatabaseManager.bans.isPlayerBanned(ip)) {
            DatabaseManager.bans.banPlayer(player, bannedBy, ip, reason, "ipban");
        }

        if (!Utilities.isIPAddress(player)) {
            GSPlayer t = PlayerManager.getSimilarPlayer(player);
            if (t.getProxiedPlayer() != null) {
                disconnectPlayer(ProxyServer.getInstance().getPlayer(player), ConfigManager.messages.IPBAN_PLAYER.replace("{message}", reason).replace("{sender}", bannedBy));
            }
        }

        if (ConfigManager.bans.BroadcastBans) {
            PlayerManager.sendBroadcast(ConfigManager.messages.IPBAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        } else {
            PlayerManager.sendMessageToPlayer(bannedBy, ConfigManager.messages.IPBAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        }
    }

    public static void kickAll(String sender, String message) {
        if (message.equals("")) {
            message = ConfigManager.messages.DEFAULT_KICK_MESSAGE;
        }

        message = Utilities.colorize(ConfigManager.messages.KICK_PLAYER_MESSAGE.replace("{message}", message).replace("{sender}", sender));

        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            disconnectPlayer(p, message);
        }
    }

    public static void checkPlayersBan(String sender, String player) {
        GSPlayer p = PlayerManager.getPlayer(sender);
        Ban b = DatabaseManager.bans.getBanInfo(player);

        if (b == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_NOT_BANNED);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
            p.sendMessage(ChatColor.DARK_AQUA + "--------" + ChatColor.DARK_RED + "Ban Info" + ChatColor.DARK_AQUA + "--------");
            p.sendMessage(ChatColor.RED + "Player: " + ChatColor.AQUA + b.getPlayer());
            p.sendMessage(ChatColor.RED + "Ban type: " + ChatColor.AQUA + b.getType());
            p.sendMessage(ChatColor.RED + "Banned by: " + ChatColor.AQUA + b.getBannedBy());
            p.sendMessage(ChatColor.RED + "Ban reason: " + ChatColor.AQUA + b.getReason());
            p.sendMessage(ChatColor.RED + "Bannned on: " + ChatColor.AQUA + sdf.format(b.getBannedOn()));

            if (b.getBannedUntil() == null) {
                p.sendMessage(ChatColor.RED + "Bannned until: " + ChatColor.AQUA + "-Forever-");
            } else {
                p.sendMessage(ChatColor.RED + "Bannned until: " + ChatColor.AQUA + sdf.format(b.getBannedUntil()));
            }
        }
    }

    public static void kickPlayer(String sender, String player, String reason) {
        if (reason.equals("")) {
            reason = ConfigManager.messages.DEFAULT_KICK_MESSAGE;
        }

        GSPlayer p = PlayerManager.getPlayer(sender);
        GSPlayer t = PlayerManager.getPlayer(player);
        if (t == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }

        disconnectPlayer(t.getProxiedPlayer(), Utilities.colorize(ConfigManager.messages.KICK_PLAYER_MESSAGE.replace("{message}", reason).replace("{sender}", sender)));
        if (ConfigManager.bans.BroadcastKicks) {
            PlayerManager.sendBroadcast(Utilities.colorize(ConfigManager.messages.KICK_PLAYER_BROADCAST.replace("{message}", reason).replace("{player}", t.getName()).replace("{sender}", sender)));
        }
    }

    public static void disconnectPlayer(ProxiedPlayer player, String message) {
        PlayerManager.unloadPlayer(player.getName());
        player.disconnect(Utilities.colorize(message));
    }

    public static void reloadBans(String sender) {
        PlayerManager.getPlayer(sender).sendMessage("Bans Reloaded");

        try {
            ConfigManager.bans.reload();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void tempBanPlayer(String sender, String player, int seconds, String message) {
        GSPlayer p = PlayerManager.getPlayer(sender);
        GSPlayer t = PlayerManager.getSimilarPlayer(player);

        if (t == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }

        if (DatabaseManager.bans.isPlayerBanned(t.getName()) || DatabaseManager.bans.isPlayerBanned(DatabaseManager.players.getPlayerIP(t.getName()))) {
            p.sendMessage(ConfigManager.messages.PLAYER_ALREADY_BANNED);
            return;
        }

        if (message.equals("")) {
            message = ConfigManager.messages.DEFAULT_BAN_REASON;
        }

        Date sqlToday = new Date(System.currentTimeMillis() + (seconds * 1000));
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd MMM yyyy HH:mm:ss");
        String time = sdf.format(sqlToday);
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");

        DatabaseManager.bans.tempBanPlayer(player, sender, player, message, sdf.format(sqlToday));


        if(t.getProxiedPlayer() != null) {
            disconnectPlayer(t.getProxiedPlayer(), ConfigManager.messages.TEMP_BAN_MESSAGE.replace("{sender}", p.getName()).replace("{time}", time).replace("{message}", message));
        }

        if (ConfigManager.bans.BroadcastBans) {
            PlayerManager.sendBroadcast(ConfigManager.messages.TEMP_BAN_BROADCAST.replace("{player}", player).replace("{sender}", p.getName()).replace("{message}", message).replace("{time}", time));
        } else {
            p.sendMessage(ConfigManager.messages.TEMP_BAN_BROADCAST.replace("{player}", player).replace("{sender}", p.getName()).replace("{message}", message).replace("{time}", time));
        }
    }

    public static boolean checkTempBan(Ban b) {
        java.util.Date today = new java.util.Date(Calendar.getInstance().getTimeInMillis());
        java.util.Date banned = b.getBannedUntil();
        if (today.compareTo(banned) >= 0) {
            DatabaseManager.bans.unbanPlayer(b.getId());
            return false;
        }

        return true;
    }
}
