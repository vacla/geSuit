package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Location;
import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TeleportManager {
    public static HashMap<BSPlayer, BSPlayer> pendingTeleportsTPA; // Player ----teleported---> player
    public static HashMap<BSPlayer, BSPlayer> pendingTeleportsTPAHere; // Player ----teleported---> player
    public static String OUTGOING_CHANNEL = "BungeeSuiteTP";
    static int expireTime;

    public static void initialise() {
        pendingTeleportsTPA = new HashMap<>();
        pendingTeleportsTPAHere = new HashMap<>();
        expireTime = ConfigManager.teleport.TeleportRequestExpireTime;
    }

    public static void requestToTeleportToPlayer(String player, String target) {
        final BSPlayer bp = PlayerManager.getPlayer(player);
        final BSPlayer bt = PlayerManager.getPlayer(target);
        if (playerHasPendingTeleport(bp)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING);
            return;
        }
        if (bt == null) {
            bp.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }
        if (!playerIsAcceptingTeleports(bt)) {
            bp.sendMessage(ConfigManager.messages.TELEPORT_UNABLE);
            return;
        }
        if (playerHasPendingTeleport(bt)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING_OTHER);
            return;
        }
        pendingTeleportsTPA.put(bt, bp);
        bp.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_SENT);
        bt.sendMessage(ConfigManager.messages.PLAYER_REQUESTS_TO_TELEPORT_TO_YOU.replace("{player}", bp.getName()));
        ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {
            @Override
            public void run() {
                if (pendingTeleportsTPA.containsKey(bt)) {
                    if (!pendingTeleportsTPA.get(bt).equals(bp)) {
                        return;
                    }
                    if (bp != null) {
                        bp.sendMessage(ConfigManager.messages.TPA_REQUEST_TIMED_OUT.replace("{player}", bt.getName()));
                    }
                    pendingTeleportsTPA.remove(bt);
                    if (bt != null) {
                        bt.sendMessage(ConfigManager.messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getName()));
                    }
                }
            }
        }, expireTime, TimeUnit.SECONDS);
    }

    public static void requestPlayerTeleportToYou(String player, String target) {
        final BSPlayer bp = PlayerManager.getPlayer(player);
        final BSPlayer bt = PlayerManager.getPlayer(target);
        if (playerHasPendingTeleport(bp)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING);
            return;
        }
        if (bt == null) {
            bp.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }
        if (!playerIsAcceptingTeleports(bt)) {
            bp.sendMessage(ConfigManager.messages.TELEPORT_UNABLE);
            return;
        }
        if (playerHasPendingTeleport(bt)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING_OTHER);
            return;
        }
        pendingTeleportsTPAHere.put(bt, bp);
        bp.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_SENT);
        bt.sendMessage(ConfigManager.messages.PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM.replace("{player}", bp.getName()));
        ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable() {
            @Override
            public void run() {
                if (pendingTeleportsTPAHere.containsKey(bt)) {
                    if (!pendingTeleportsTPAHere.get(bt).equals(bp)) {
                        return;
                    }
                    if (bp != null) {
                        bp.sendMessage(ConfigManager.messages.TPAHERE_REQUEST_TIMED_OUT.replace("{player}", bt.getName()));
                    }
                    pendingTeleportsTPAHere.remove(bt);
                    if (bt != null) {
                        bt.sendMessage(ConfigManager.messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getName()));
                    }
                }
            }
        }, expireTime, TimeUnit.SECONDS);
    }

    public static void acceptTeleportRequest(BSPlayer player) {
        if (pendingTeleportsTPA.containsKey(player)) {
            BSPlayer target = pendingTeleportsTPA.get(player);
            target.sendMessage(ConfigManager.messages.TELEPORTED_TO_PLAYER.replace("{player}", player.getName()));
            player.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", target.getName()));
            teleportPlayerToPlayer(target, player);
            pendingTeleportsTPA.remove(player);
        } else if (pendingTeleportsTPAHere.containsKey(player)) {
            BSPlayer target = pendingTeleportsTPAHere.get(player);
            player.sendMessage(ConfigManager.messages.TELEPORTED_TO_PLAYER.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", player.getName()));
            teleportPlayerToPlayer(player, target);
            pendingTeleportsTPAHere.remove(player);
        } else {
            player.sendMessage(ConfigManager.messages.NO_TELEPORTS);
        }
    }

    public static void denyTeleportRequest(BSPlayer player) {
        if (pendingTeleportsTPA.containsKey(player)) {
            BSPlayer target = pendingTeleportsTPA.get(player);
            player.sendMessage(ConfigManager.messages.TELEPORT_DENIED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_DENIED.replace("{player}", player.getName()));
            pendingTeleportsTPA.remove(player);
        } else if (pendingTeleportsTPAHere.containsKey(player)) {
            BSPlayer target = pendingTeleportsTPAHere.get(player);
            player.sendMessage(ConfigManager.messages.TELEPORT_DENIED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_DENIED.replace("{player}", player.getName()));
            pendingTeleportsTPAHere.remove(player);
        } else {
            player.sendMessage(ConfigManager.messages.NO_TELEPORTS);
        }
    }

    public static boolean playerHasPendingTeleport(BSPlayer player) {
        return pendingTeleportsTPA.containsKey(player) || pendingTeleportsTPAHere.containsKey(player);
    }

    public static boolean playerIsAcceptingTeleports(BSPlayer player) {
        return player.acceptingTeleports();
    }

    public static void setPlayersDeathBackLocation(BSPlayer player, Location loc) {
        player.setDeathBackLocation(loc);
    }

    public static void setPlayersTeleportBackLocation(BSPlayer player, Location loc) {
        if (player != null) {
            player.setTeleportBackLocation(loc);
        }
    }

    public static void sendPlayerToLastBack(BSPlayer player, boolean death, boolean teleport) {
        if (player.hasDeathBackLocation() || player.hasTeleportBackLocation()) {
            player.sendMessage(ConfigManager.messages.SENT_BACK);
        } else {
            player.sendMessage(ConfigManager.messages.NO_BACK_TP);
        }
        if (death && teleport) {
            if (player.hasDeathBackLocation() || player.hasTeleportBackLocation()) {
                teleportPlayerToLocation(player, player.getLastBackLocation());
            }
        } else if (death) {
            teleportPlayerToLocation(player, player.getDeathBackLocation());
        } else if (teleport) {
            teleportPlayerToLocation(player, player.getTeleportBackLocation());
        }
    }

    public static void togglePlayersTeleports(BSPlayer player) {
        if (player.acceptingTeleports()) {
            player.setAcceptingTeleports(false);
            player.sendMessage(ConfigManager.messages.TELEPORT_TOGGLE_OFF);
        } else {
            player.setAcceptingTeleports(true);
            player.sendMessage(ConfigManager.messages.TELEPORT_TOGGLE_ON);
        }
    }

    public static void teleportPlayerToPlayer(BSPlayer p, BSPlayer t) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("TeleportToPlayer");
            out.writeUTF(p.getName());
            out.writeUTF(t.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPluginMessageTaskTP(t.getServer().getInfo(), b);
        if (!p.getServer().getInfo().equals(t.getServer().getInfo())) {
            p.getProxiedPlayer().connect(t.getServer().getInfo());
        }
    }

    public static void tpAll(String sender, String target) {
        BSPlayer p = PlayerManager.getPlayer(sender);
        BSPlayer t = PlayerManager.getPlayer(target);
        if (t == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }
        for (BSPlayer player : PlayerManager.getPlayers()) {
            if (!player.equals(p)) {
                teleportPlayerToPlayer(player, t);
            }
            player.sendMessage(ConfigManager.messages.ALL_PLAYERS_TELEPORTED.replace("{player}", t.getName()));
        }
    }

    public static void teleportPlayerToLocation(final BSPlayer p, final Location t) {
        if (!p.getServer().getInfo().equals(t.getServer())) {
            BackTeleportManager.put(p.getProxiedPlayer(), t);
            p.getProxiedPlayer().connect(t.getServer());
        } else {
            sendTeleportPlayerToLocation(p.getProxiedPlayer(), t);
        }
    }

    public static void sendTeleportPlayerToLocation(ProxiedPlayer p, Location t) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("TeleportToLocation");
            out.writeUTF(p.getName());
            out.writeUTF(t.getWorld());
            out.writeDouble(t.getX());
            out.writeDouble(t.getY());
            out.writeDouble(t.getZ());
            out.writeFloat(t.getYaw());
            out.writeFloat(t.getPitch());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendPluginMessageTaskTP(t.getServer(), b);
    }

    public static void sendPluginMessageTaskTP(ServerInfo server, ByteArrayOutputStream b) {
        BungeeSuite.proxy.getScheduler().runAsync(BungeeSuite.instance, new SendPluginMessage(OUTGOING_CHANNEL, server, b));
    }

    public static void teleportPlayerToPlayer(String sender, String player, String target, boolean silent, boolean bypass) {
        BSPlayer s = PlayerManager.getPlayer(sender);
        BSPlayer p = PlayerManager.getPlayer(player);
        BSPlayer t = PlayerManager.getPlayer(target);
        if (p == null || t == null) {
            s.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }
        if (!bypass) {
            if (!playerIsAcceptingTeleports(p) || !playerIsAcceptingTeleports(t)) {
                s.sendMessage(ConfigManager.messages.TELEPORT_UNABLE);
                return;
            }
        }
        if (!(sender.equals(player) || sender.equals(target))) {
            s.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED.replace("{player}", p.getName()).replace("{target}", t.getName()));
        }
        teleportPlayerToPlayer(p, t);
        if (!silent) {
            t.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", p.getName()));
        }
        p.sendMessage(ConfigManager.messages.TELEPORTED_TO_PLAYER.replace("{player}", t.getName()));
    }

}


