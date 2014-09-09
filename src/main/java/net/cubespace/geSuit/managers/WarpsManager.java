package net.cubespace.geSuit.managers;

import java.util.HashMap;
import java.util.List;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Warp;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;
import net.md_5.bungee.api.ChatColor;

public class WarpsManager {
    private static HashMap<String, Warp> warps = new HashMap<>();

    public static void loadWarpLocations() {
        List<Warp> warps1 = DatabaseManager.warps.getWarps();

        for(Warp warp : warps1) {
            warps.put(warp.getName().toLowerCase(), warp);
        }
    }

    public static void setWarp(GSPlayer sender, String name, Location loc, boolean hidden, boolean global) {
        Warp w;
        if (doesWarpExist(name)) {
            w = warps.get(name.toLowerCase());
            w.setLocation(loc);
            w.setGlobal(global);
            w.setHidden(hidden);
            DatabaseManager.warps.updateWarp(w);
            sender.sendMessage(ConfigManager.messages.WARP_UPDATED.replace("{warp}", name));
        } else {
            w = new Warp(name, loc, hidden, global);
            warps.put(name.toLowerCase(), w);
            DatabaseManager.warps.insertWarp(w);
            sender.sendMessage(ConfigManager.messages.WARP_CREATED.replace("{warp}", name));
        }
    }

    public static void deleteWarp(GSPlayer sender, String warp) {
        Warp w = getWarp(warp);
        warps.remove(w.getName().toLowerCase());
        DatabaseManager.warps.deleteWarp(w.getName());
        sender.sendMessage(ConfigManager.messages.WARP_DELETED.replace("{warp}", warp));
    }

    public static Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public static boolean doesWarpExist(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public static void getWarpsList(String sender, boolean server, boolean global, boolean hidden, boolean bypass) {
        GSPlayer s = PlayerManager.getPlayer(sender);
        if (!(server || global || hidden)) {
            s.sendMessage(ChatColor.RED + "No warps to display");
        }
        String serverString = ChatColor.GOLD + "Server warps: \n";
        String globalString = ChatColor.GOLD + "Global warps: \n";
        String hiddenString = ChatColor.GOLD + "Hidden warps: \n";
        for (Warp w : warps.values()) {
            if (w.isGlobal()) {
                globalString += w.getName() + ", ";
            } else if (w.isHidden()) {
                hiddenString += w.getName() + ", ";
            } else if (s.getServer().equals(w.getLocation().getServer().getName())) {
                serverString += w.getName() + ", ";
            } else if (bypass) {
                globalString += w.getName() + ", ";
            }
        }
        if (server) {
            if (serverString.length() == 17) {
                serverString += ChatColor.RED + " none  ";
            }
            s.sendMessage(serverString.substring(0, serverString.length() - 2));
        }
        if (global) {
            if (globalString.length() == 17) {
                globalString += ChatColor.RED + " none  ";
            }

            s.sendMessage(globalString.substring(0, globalString.length() - 2));
        }
        if (hidden) {
            if (hiddenString.length() == 17) {
                hiddenString += ChatColor.RED + " none  ";
            }
            s.sendMessage(hiddenString.substring(0, hiddenString.length() - 2));
        }
    }


    public static void sendPlayerToWarp(String sender, String player, String warp, boolean permission, boolean bypass) {
        GSPlayer s = PlayerManager.getPlayer(sender);
        GSPlayer p = PlayerManager.getPlayer(player);
        if (p == null) {
            s.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }

        Warp w = warps.get(warp.toLowerCase());
        if (w == null) {
            s.sendMessage(ConfigManager.messages.WARP_DOES_NOT_EXIST.replace("{warp}", warp));
            return;
        }

        if (!permission) {
            s.sendMessage(ChatColor.translateAlternateColorCodes('&',ConfigManager.messages.WARP_NO_PERMISSION));
            return;
        }

        if (!w.isGlobal() && !w.isHidden()) {
            if (!w.getLocation().getServer().getName().equals(p.getServer()) && !bypass) {
                s.sendMessage(ConfigManager.messages.WARP_SERVER);
                return;
            }
        }

        Location l = w.getLocation();
        p.sendMessage(ConfigManager.messages.PLAYER_WARPED.replace("{warp}", w.getName()));
        if (!p.equals(s)) {
            s.sendMessage(ConfigManager.messages.PLAYER_WARPED_OTHER.replace("{player}", p.getName()).replace("{warp}", w.getName()));
        }

        TeleportToLocation.execute(p, l);
    }
}

