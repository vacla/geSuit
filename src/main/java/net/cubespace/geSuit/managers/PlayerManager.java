package net.cubespace.geSuit.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerManager
{

    public static HashMap<String, GSPlayer> onlinePlayers = new HashMap<>();
    public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<>();

    public static boolean playerExists(ProxiedPlayer player, boolean uuid)
    {
        return getPlayer(player.getName()) != null
                || (uuid) ? DatabaseManager.players.playerExists(player.getUUID()) : DatabaseManager.players.playerExists(player.getName());
    }

    public static void loadPlayer(ProxiedPlayer player)
    {
        if (playerExists(player, FeatureDetector.canUseUUID())) {
            boolean tps;

            if (FeatureDetector.canUseUUID()) {
                tps = DatabaseManager.players.getPlayerTPS(player.getName());
            }
            else {
                tps = DatabaseManager.players.getPlayerTPS(player.getName());
            }

            GSPlayer gsPlayer = new GSPlayer(player.getName(), (FeatureDetector.canUseUUID()) ? player.getUUID() : null, tps);
            onlinePlayers.put(player.getName(), gsPlayer);

            DatabaseManager.players.updatePlayer(gsPlayer);

            LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", gsPlayer.getName()));

            HomesManager.loadPlayersHomes(gsPlayer);
        }
        else {
            createNewPlayer(player);
        }
    }

    private static void createNewPlayer(final ProxiedPlayer player)
    {
        String ip = player.getAddress().getAddress().toString();
        final GSPlayer gsPlayer = new GSPlayer(player.getName(), (FeatureDetector.canUseUUID()) ? player.getUUID() : null, true);

        DatabaseManager.players.insertPlayer(gsPlayer, ip.substring(1, ip.length()));

        if (ConfigManager.main.NewPlayerBroadcast) {
            sendBroadcast(ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()));
        }

        onlinePlayers.put(player.getName(), gsPlayer);
        LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", gsPlayer.getName()));

        if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
            SpawnManager.newPlayers.add(player);

            ProxyServer.getInstance().getScheduler().schedule(geSuit.instance, new Runnable()
            {

                @Override
                public void run()
                {
                    SpawnManager.sendPlayerToNewPlayerSpawn(gsPlayer);
                    SpawnManager.newPlayers.remove(player);
                }

            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    public static void unloadPlayer(String player)
    {
        if (onlinePlayers.containsKey(player)) {
            onlinePlayers.remove(player);

            LoggingManager.log(ConfigManager.messages.PLAYER_UNLOAD.replace("{player}", player));
        }
    }

    public static void sendMessageToTarget(CommandSender target, String message)
    {
        // Shouldnt need it. But let's be cautious.
        if (target == null) {
            return;
        }

        // Not exactly sure where we use the new line besides in the soon-to-be-removed MOTD...
        for (String line : Utilities.colorize(message).split("\n")) {
            target.sendMessage(line);
        }
    }
    
    public static void sendMessageToTarget(GSPlayer target, String message)
    {
        sendMessageToTarget(target.getProxiedPlayer(), message);
    }

    public static void sendMessageToTarget(String target, String message)
    {
        sendMessageToTarget(getPlayer(target) != null ? getPlayer(target).getProxiedPlayer() : ProxyServer.getInstance().getConsole(), message);
    }

    public static void sendBroadcast(String message)
    {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            sendMessageToTarget(p.getName(), message);
        }
        LoggingManager.log(message);
    }

    public static GSPlayer getSimilarPlayer(String player)
    {
        if (player == null) {
            Exception exception = new Exception("test");
            exception.printStackTrace();
            geSuit.instance.getLogger().severe("getSimilarPlayer() player is null");
            return null;
        }

        for (GSPlayer p : onlinePlayers.values()) {
            if ((p.getProxiedPlayer() != null && p.getProxiedPlayer().getDisplayName() != null && p.getProxiedPlayer().getDisplayName().toLowerCase().startsWith(player.toLowerCase())) || p.getName().toLowerCase().startsWith(player.toLowerCase()) || (p.getUuid() != null && p.getUuid().equals(player))) {
                return p;
            }
        }

        return null;
    }

    public static List<GSPlayer> getPlayersByIP(String ip)
    {
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

    public static Collection<GSPlayer> getPlayers()
    {
        return onlinePlayers.values();
    }

    public static GSPlayer getPlayer(String player)
    {
        return onlinePlayers.get(player);
    }

}
