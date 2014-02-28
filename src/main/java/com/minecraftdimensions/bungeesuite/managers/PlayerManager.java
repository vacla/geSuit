package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    public static HashMap<String, BSPlayer> onlinePlayers = new HashMap<>();
    public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<>();

    public static boolean playerExists(String player) {
        return getPlayer(player) != null ||
                SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE playername = '" + player + "'");
    }

    public static void loadPlayer(ProxiedPlayer player) throws SQLException {
        boolean tps = true;

        if (playerExists(player.getName())) {
            ResultSet res = SQLManager.sqlQuery("SELECT tps FROM BungeePlayers WHERE playername = '" + player + "'");
            while (res.next()) {
                tps = res.getBoolean("tps");
            }
            res.close();

            BSPlayer bsplayer = new BSPlayer(player.getName(), tps);
            addPlayer(bsplayer);

            HomesManager.loadPlayersHomes(bsplayer);
        } else {
            createNewPlayer(player);
        }
    }

    private static void createNewPlayer(final ProxiedPlayer player) throws SQLException {
        String ip = player.getAddress().getAddress().toString();
        SQLManager.standardQuery("INSERT INTO BungeePlayers (playername,lastonline,ipaddress,channel) VALUES ('" + player.getName() + "', NOW(), '" + ip.substring(1, ip.length()) + "','')");

        final BSPlayer bsplayer = new BSPlayer(player.getName(), true);
        if (ConfigManager.main.NewPlayerBroadcast) {
            sendBroadcast(ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()));
        }

        addPlayer(bsplayer);

        if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
            SpawnManager.newPlayers.add(player);
            ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {

                @Override
                public void run() {
                    SpawnManager.sendPlayerToNewPlayerSpawn(bsplayer, true);
                    SpawnManager.newPlayers.remove(player);
                }

            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    private static void addPlayer(BSPlayer player) {
        onlinePlayers.put(player.getName(), player);

        LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", player.getName()));
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

    public static String getPlayersIP(String player) throws SQLException {
        BSPlayer p = getPlayer(player);
        String ip = null;

        if (p == null) {
            ResultSet res = SQLManager.sqlQuery("SELECT ipaddress FROM BungeePlayers WHERE playername = '" + player + "'");
            while (res.next()) {
                ip = res.getString("ipaddress");
            }
            res.close();
        } else {
            ip = p.getProxiedPlayer().getAddress().getAddress().toString();
            ip = ip.substring(1, ip.length());
        }

        return ip;
    }

    public static void sendBroadcast(String message) {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            for (String line : message.split("\n")) {
                p.sendMessage(line);
            }
        }

        LoggingManager.log(message);
    }

    public static ArrayList<String> getPlayersAltAccounts(String player) throws SQLException {
        ArrayList<String> accounts = new ArrayList<>();
        ResultSet res = SQLManager.sqlQuery("SELECT playername from BungeePlayers WHERE ipaddress = (SELECT ipaddress FROM BungeePlayers WHERE playername = '" + player + "')");

        while (res.next()) {
            accounts.add(res.getString("playername"));
        }

        res.close();
        return accounts;
    }

    public static ArrayList<String> getPlayersAltAccountsByIP(String ip) throws SQLException {
        ArrayList<String> accounts = new ArrayList<>();
        ResultSet res = SQLManager.sqlQuery("SELECT playername from BungeePlayers WHERE ipaddress = '" + ip + "'");

        while (res.next()) {
            accounts.add(res.getString("playername"));
        }

        res.close();
        return accounts;
    }

    private static void sendServerMessage(Server server, String message) {
        for (ProxiedPlayer p : server.getInfo().getPlayers()) {
            for (String line : message.split("\n")) {
                p.sendMessage(line);
            }
        }
    }

    public static boolean isPlayerOnline(String player) {
        return onlinePlayers.containsKey(player);
    }

    public static Collection<BSPlayer> getPlayers() {
        return onlinePlayers.values();
    }

    public static BSPlayer getPlayer(String player) {
        return onlinePlayers.get(player);
    }

    public static BSPlayer getPlayer(CommandSender sender) {
        return onlinePlayers.get(sender.getName());
    }
}
