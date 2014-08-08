package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.events.NewPlayerJoinEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerManager {

    public static HashMap<String, GSPlayer> onlinePlayers = new HashMap<>();
    public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<>();

    public static boolean playerExists(ProxiedPlayer player, boolean uuid) {
        return getPlayer(player.getName()) != null
                || (uuid) ? DatabaseManager.players.playerExists(player.getUUID()) : DatabaseManager.players.playerExists(player.getName());
    }

    public static GSPlayer loadPlayer(ProxiedPlayer player) {
        if (playerExists(player, FeatureDetector.canUseUUID())) {
            boolean tps;

            if (FeatureDetector.canUseUUID()) {
                tps = DatabaseManager.players.getPlayerTPS(player.getUUID());
            } else {
                tps = DatabaseManager.players.getPlayerTPS(player.getName());
            }

            GSPlayer gsPlayer = new GSPlayer(player.getName(), (FeatureDetector.canUseUUID()) ? player.getUUID() : null, tps, player.getAddress().getHostString());
            onlinePlayers.put(player.getName(), gsPlayer);

            DatabaseManager.players.updatePlayer(gsPlayer);

            LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", gsPlayer.getName()).replace("{uuid}", player.getUniqueId().toString()));

            HomesManager.loadPlayersHomes(gsPlayer);
            
            return gsPlayer;
        } else {
            return createNewPlayer(player);
        }
    }

    private static GSPlayer createNewPlayer(final ProxiedPlayer player) {
        String ip = player.getAddress().getAddress().toString();
        final GSPlayer gsPlayer = new GSPlayer(player.getName(), (FeatureDetector.canUseUUID()) ? player.getUUID() : null, true);

        onlinePlayers.put(player.getName(), gsPlayer);
        DatabaseManager.players.insertPlayer(gsPlayer, ip.substring(1, ip.length()));

        LoggingManager.log(ConfigManager.messages.PLAYER_CREATE.replace("{player}", player.getName()).replace("{uuid}", player.getUniqueId().toString()));

        if (ConfigManager.main.NewPlayerBroadcast) {
            String welcomeMsg = null;
            sendBroadcast(welcomeMsg = ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()));
            // Firing custom event
            ProxyServer.getInstance().getPluginManager().callEvent(new NewPlayerJoinEvent(player.getName(), welcomeMsg));
        }

        if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
            SpawnManager.newPlayers.add(player);

            ProxyServer.getInstance().getScheduler().schedule(geSuit.instance, new Runnable() {

                @Override
                public void run() {
                    SpawnManager.sendPlayerToNewPlayerSpawn(gsPlayer);
                    SpawnManager.newPlayers.remove(player);
                }

            }, 300, TimeUnit.MILLISECONDS);
        }
        
        return gsPlayer;
    }

    public static void unloadPlayer(String player) {
        if (onlinePlayers.containsKey(player)) {
            onlinePlayers.remove(player);

            LoggingManager.log(ConfigManager.messages.PLAYER_UNLOAD.replace("{player}", player));
        }
    }

    public static void sendMessageToTarget(CommandSender target, String message) {
        // Shouldnt need it. But let's be cautious.
        if (target == null) {
        	LoggingManager.log("WARNING: sendMessageToTarget(CommandSender, String): Target is null!");
        	return;
        }

        // Not exactly sure where we use the new line besides in the soon-to-be-removed MOTD...
        for (String line : Utilities.colorize(message).split("\n")) {
            if (geSuit.instance.isDebugEnabled()) {
    			geSuit.instance.getLogger().info("geSuit DEBUG: [SendMessage] " + target.getName() + ": " + Utilities.colorize(line));
    		}
            target.sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
        }
    }

    public static void sendMessageToTarget(GSPlayer target, String message) {
        if (target == null) {
        	LoggingManager.log("WARNING: sendMessageToTarget(GSPlayer, String): Target is null!");
        	return;
        }
        sendMessageToTarget(target.getProxiedPlayer(), message);
    }

    public static void sendMessageToTarget(String target, String message) {
        if ((target == null) || (target.isEmpty())) {
        	LoggingManager.log("WARNING: sendMessageToTarget(String, String): Target is null or empty!");
        	return;
        }
        sendMessageToTarget(getPlayer(target) != null ? getPlayer(target).getProxiedPlayer() : ProxyServer.getInstance().getConsole(), message);
    }

    public static void sendBroadcast(String message) {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            sendMessageToTarget(p.getName(), message);
        }
        LoggingManager.log(message);
    }

    public static String getLastSeeninfos(String player) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd MMM yyyy HH:mm");
        GSPlayer p = getPlayer(player);
        if (p == null) { //Offline
            p = DatabaseManager.players.loadPlayer(player);
            if (p == null) //Unknown player
            {
                return ConfigManager.messages.PLAYER_DOES_NOT_EXIST;
            } else {
                return ConfigManager.messages.PLAYER_SEEN_OFFLINE
                        .replace("{player}", p.getName())
                        .replace("{ip}", p.getIp())
                        .replace("{seen}", sdf.format(p.getLastOnline()));
            }
        } else { //Online
            return ConfigManager.messages.PLAYER_SEEN_ONLINE
                    .replace("{player}", p.getName())
                    .replace("{ip}", p.getIp())
                    .replace("{server}", p.getServer());
        }
    }

    public static GSPlayer getSimilarPlayer(String player) {
        for (GSPlayer p : onlinePlayers.values()) {
            if ((p.getProxiedPlayer() != null && p.getProxiedPlayer().getDisplayName() != null && p.getProxiedPlayer().getDisplayName().toLowerCase().startsWith(player.toLowerCase())) || p.getName().toLowerCase().startsWith(player.toLowerCase()) || (p.getUuid() != null && p.getUuid().equals(player))) {
                return p;
            }
        }

        return null;
    }

    public static List<GSPlayer> getPlayersByIP(String ip) {
        List<GSPlayer> matchingPlayers = new ArrayList<GSPlayer>();
        if (ip == null) {
            Exception exception = new Exception("test");
            exception.printStackTrace();
            geSuit.instance.getLogger().severe("getPlayersByIP() ip is null");
            return null;
        }

        for (GSPlayer p : onlinePlayers.values()) {
            if (p.getProxiedPlayer().getAddress().getHostString().equalsIgnoreCase(ip)) {
                matchingPlayers.add(p);
            }
        }

        return matchingPlayers;
    }

    public static Collection<GSPlayer> getPlayers() {
        return onlinePlayers.values();
    }

    public static GSPlayer getPlayer(String player) {
        return onlinePlayers.get(player);
    }

    public static String getLastSeeninfosStripped(String player) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd MMM yyyy HH:mm");
        GSPlayer p = getPlayer(player);
        if (p == null) { //Offline
            p = DatabaseManager.players.loadPlayer(player);
            if (p == null) //Unknown player
            {
                return ConfigManager.messages.PLAYER_DOES_NOT_EXIST;
            } else {
                return ConfigManager.messages.PLAYER_SEEN_OFFLINE
                        .replace("{player}", p.getName())
                        .replace("{ip}", "[Stripped]")
                        .replace("{seen}", sdf.format(p.getLastOnline()));
            }
        } else { //Online
            return ConfigManager.messages.PLAYER_SEEN_ONLINE
                    .replace("{player}", p.getName())
                    .replace("{ip}","[Stripped]")
                    .replace("{server}", p.getServer());
        }
    }

}
