package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    public static HashMap<String, GSPlayer> onlinePlayers = new HashMap<>();
    public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<>();

    public static boolean playerExists(String player) {
        return getPlayer(player) != null || DatabaseManager.players.playerExists(player);
    }

    public static void loadPlayer(ProxiedPlayer player) {
        if (playerExists(player.getName())) {
            boolean tps = DatabaseManager.players.getPlayerTPS(player.getName());

            GSPlayer bsplayer = new GSPlayer(player.getName(), tps);
            onlinePlayers.put(bsplayer.getName(), bsplayer);
            LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", bsplayer.getName()));

            HomesManager.loadPlayersHomes(bsplayer);
        } else {
            createNewPlayer(player);
        }
    }

    private static void createNewPlayer(final ProxiedPlayer player) {
        String ip = player.getAddress().getAddress().toString();

        DatabaseManager.players.insertPlayer(player.getName(), ip.substring(1, ip.length()));

        final GSPlayer bsplayer = new GSPlayer(player.getName(), true);
        if (ConfigManager.main.NewPlayerBroadcast) {
            sendBroadcast(ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()));
        }

        onlinePlayers.put(bsplayer.getName(), bsplayer);
        LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", bsplayer.getName()));

        if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
            SpawnManager.newPlayers.add(player);
            ProxyServer.getInstance().getScheduler().schedule(geSuit.instance, new Runnable() {

                @Override
                public void run() {
                    SpawnManager.sendPlayerToNewPlayerSpawn(bsplayer);
                    SpawnManager.newPlayers.remove(player);
                }

            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    public static void unloadPlayer(String player) {
        if (onlinePlayers.containsKey(player)) {
            onlinePlayers.remove(player);
            LoggingManager.log(ConfigManager.messages.PLAYER_UNLOAD.replace("{player}", player));
        }
    }

    public static void sendMessageToPlayer(String player, String message) {
        if (player.equals("CONSOLE")) {
            ProxyServer.getInstance().getConsole().sendMessage(message);
        } else {
            for (String line : message.split("\n")) {
                getPlayer(player).sendMessage(line);
            }
        }
    }

    public static void sendBroadcast(String message) {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            for (String line : message.split("\n")) {
                p.sendMessage(Utilities.colorize(line));
            }
        }

        LoggingManager.log(message);
    }

    public static boolean isPlayerOnline(String player) {
        return onlinePlayers.containsKey(player);
    }

    public static Collection<GSPlayer> getPlayers() {
        return onlinePlayers.values();
    }

    public static GSPlayer getPlayer(String player) {
        return onlinePlayers.get(player);
    }

    public static GSPlayer getPlayer(CommandSender sender) {
        return onlinePlayers.get(sender.getName());
    }
}
