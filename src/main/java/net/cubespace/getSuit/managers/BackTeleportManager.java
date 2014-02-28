package net.cubespace.getSuit.managers;

import net.cubespace.getSuit.objects.Location;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.LinkedHashMap;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class BackTeleportManager {
    private static LinkedHashMap<ProxiedPlayer, Location> teleportLocations = new LinkedHashMap<>();

    public static boolean contains(ProxiedPlayer proxiedPlayer) {
        return teleportLocations.containsKey(proxiedPlayer);
    }

    public static void put(ProxiedPlayer proxiedPlayer, Location location) {
        teleportLocations.put(proxiedPlayer, location);
    }

    public static void remove(ProxiedPlayer proxiedPlayer) {
        teleportLocations.remove(proxiedPlayer);
    }

    public static Location get(ProxiedPlayer player) {
        return teleportLocations.get(player);
    }
}
