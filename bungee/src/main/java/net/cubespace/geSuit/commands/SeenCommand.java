package net.cubespace.geSuit.commands;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import au.com.addstar.bc.BungeeChat;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.config.MainConfig;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.general.GeoIPLookup;
import net.cubespace.geSuit.moderation.BanManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /seen
 * Permission needed: gesuit.seen or gesuit.admin
 * Arguments: none
 * What does it do: Displays <player> last online time
 */
public class SeenCommand extends Command {
    private BanManager bans;
    private GeoIPLookup geoipLookup;
    private MainConfig config;
    
    public SeenCommand(BanManager bans, GeoIPLookup geoipLookup, MainConfig config) {
        super("seen");
        this.bans = bans;
        this.geoipLookup = geoipLookup;
        this.config = config;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.seen") || sender.hasPermission("gesuit.admin"))) {
            sender.sendMessage(Global.getMessages().get("player.no-permission"));
            return;
        }
        
        if (args.length != 1) {
            sender.sendMessage("/seen <player>");
            return;
        }
        
        GlobalPlayer player = Global.getOfflinePlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", args[0]));
            return;
        }
        
        boolean seeExtra = sender.hasPermission("gesuit.seen.extra");
        boolean seeVanish = sender.hasPermission("gesuit.seen.vanish");
        
        ProxiedPlayer pPlayer = ProxyServer.getInstance().getPlayer(player.getUniqueId());
        boolean online = pPlayer != null;

        // Vanished and not online
        if (config.BungeeChatIntegration && !seeVanish) {
            if (BungeeChat.instance.getSyncManager().getPropertyBoolean(pPlayer, "VNP:vanished", false) && !BungeeChat.instance.getSyncManager().getPropertyBoolean(pPlayer, "VNP:online", true)) {
                online = false;
            }
        }
        
        Map<String, Object> items = Maps.newLinkedHashMap();

        // Do a ban check
        if (player.isBanned()) {
            BanInfo<GlobalPlayer> nameBan = player.getBanInfo();
            if (nameBan.isTemporary()) {
                if (nameBan.getUntil() > System.currentTimeMillis()) {
                    items.put("Temp Banned", Utilities.buildShortTimeDiffString(nameBan.getUntil() - System.currentTimeMillis(), 3) + " remaining");
                    items.put(" Reason", nameBan.getReason());
                    if (seeExtra) {
                        items.put(" By", nameBan.getBannedBy());
                    }
                }
            } else {
                items.put("Banned", nameBan.getReason());
                if (seeExtra) {
                    items.put(" By", nameBan.getBannedBy());
                }
            }
        }
        
        // IP ban check
        BanInfo<InetAddress> ipBan = bans.getBan(player.getAddress());
        if (ipBan != null) {
            if (ipBan.isTemporary()) {
                if (ipBan.getUntil() > System.currentTimeMillis()) {
                    items.put("Temp IP Banned", Utilities.buildShortTimeDiffString(ipBan.getUntil() - System.currentTimeMillis(), 3) + " remaining");
                    items.put(" Reason", ipBan.getReason());
                    if (seeExtra) {
                        items.put(" By", ipBan.getBannedBy());
                    }
                }
            } else {
                items.put("IP Banned", ipBan.getReason());
                if (seeExtra) {
                    items.put(" By", ipBan.getBannedBy());
                }
            }
        }

        if (seeExtra) {
            if (online && pPlayer.getServer() != null) {
                items.put("Server", pPlayer.getServer().getInfo().getName());
            }
            items.put("IP", player.getAddress().getHostAddress());

            // Do GeoIP lookup
            String location = geoipLookup.lookup(player.getAddress());

            if (location != null) {
                items.put("Location", location);
            }
        }

        // Create header
        String messageId = (online ? "seen.header.online" : "seen.header.offline");
        
        String fullDate;
        String diff;
        
        if (online) {
            fullDate = String.format("%s @ %s", DateFormat.getDateInstance(DateFormat.MEDIUM).format(player.getSessionJoin()), DateFormat.getTimeInstance(DateFormat.MEDIUM).format(player.getSessionJoin()));
            diff = Utilities.buildTimeDiffString(System.currentTimeMillis() - player.getSessionJoin(), 2);
        } else {
            if (player.getFirstJoined() != 0) {
                fullDate = String.format("%s @ %s", DateFormat.getDateInstance(DateFormat.MEDIUM).format(player.getLastOnline()), DateFormat.getTimeInstance(DateFormat.MEDIUM).format(player.getLastOnline()));
                diff = Utilities.buildTimeDiffString(System.currentTimeMillis() - player.getLastOnline(), 2);
            } else {
                fullDate = diff = "Never";
            }
        }
        
        String message = Global.getMessages().get(
                messageId,
                "player", player.getDisplayName(),
                "timediff", diff,
                "date", fullDate
                );

        StringBuilder builder = new StringBuilder();
        builder.append(message);
        // Format each item
        for (Entry<String, Object> item : items.entrySet()) {
            builder.append('\n');
            builder.append(Global.getMessages().get("seen.format", "name", item.getKey(), "value", item.getValue()));
        }

        sender.sendMessage(builder.toString());
    }
}
