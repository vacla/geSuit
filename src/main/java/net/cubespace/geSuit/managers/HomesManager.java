package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Home;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class HomesManager {
    public static void createNewHome(String player, int serverLimit, int globalLimit, String home, Location loc) {
        GSPlayer p = PlayerManager.getPlayer(player);

        if (getHome(p, home) == null) {
            int globalHomeCount = getPlayersGlobalHomeCount(p);
            int serverHomeCount = getPlayersServerHomeCount(p);

            if (globalHomeCount >= globalLimit) {
                p.sendMessage(ConfigManager.messages.NO_HOMES_ALLOWED_GLOBAL);
                return;
            }

            if (serverHomeCount >= serverLimit) {
                p.sendMessage(ConfigManager.messages.NO_HOMES_ALLOWED_SERVER);
                return;
            }

            if (p.getHomes().get(p.getServer().getInfo().getName()) == null) {
                p.getHomes().put(p.getServer().getInfo().getName(), new ArrayList<Home>());
            }

            Home homeObject = new Home(p.getName(), home, loc);
            p.getHomes().get(p.getServer().getInfo().getName()).add(homeObject);
            DatabaseManager.homes.addHome(homeObject);

            p.sendMessage(ConfigManager.messages.HOME_SET);
        } else {
            Home home1 = getHome(p, home);
            home1.setLoc(loc);
            DatabaseManager.homes.updateHome(home1);

            p.sendMessage(ConfigManager.messages.HOME_UPDATED);
        }
    }

    private static int getPlayersGlobalHomeCount(GSPlayer player) {
        int count = 0;

        for (ArrayList<Home> list : player.getHomes().values()) {
            count += list.size();
        }

        return count;
    }

    private static int getPlayersServerHomeCount(GSPlayer player) {
        ArrayList<Home> list = player.getHomes().get(player.getServer().getInfo().getName());

        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    public static void listPlayersHomes(GSPlayer player) {
        if (player.getHomes().isEmpty()) {
            player.sendMessage(ConfigManager.messages.NO_HOMES);
            return;
        }

        boolean empty = true;
        for (String server : player.getHomes().keySet()) {
            String homes;

            if (server.equals(player.getServer().getInfo().getName())) {
                homes = ChatColor.RED + server + ": " + ChatColor.BLUE;
            } else {
                homes = ChatColor.GOLD + server + ": " + ChatColor.BLUE;
            }

            for (Home h : player.getHomes().get(server)) {
                homes += h.name + ", ";
                empty = false;
            }

            if (empty) {
                player.sendMessage(ConfigManager.messages.NO_HOMES);
                return;
            }

            player.sendMessage(homes.substring(0, homes.length() - 2));
        }

    }

    public static void loadPlayersHomes(GSPlayer player) {
        List<Home> homes = DatabaseManager.homes.getHomesForPlayer(player.getName());

        for(Home home : homes) {
            if (player.getHomes().get(home.loc.getServer().getName()) == null) {
                ArrayList<Home> list = new ArrayList<>();
                list.add(home);
                player.getHomes().put(home.loc.getServer().getName(), list);
            } else {
                player.getHomes().get(home.loc.getServer().getName()).add(home);
            }
        }
    }


    public static Home getHome(GSPlayer player, String home) {
        for (ArrayList<Home> list : player.getHomes().values()) {
            for (Home h : list) {
                if (h.name.toLowerCase().equals(home.toLowerCase())) {
                    return h;
                }
            }
        }

        return null;
    }

    public static void sendPlayerToHome(GSPlayer player, String home) {
        Home h = getHome(player, home);
        if (h == null) {
            player.sendMessage(ConfigManager.messages.HOME_DOES_NOT_EXIST);
            return;
        }

        player.sendMessage(ConfigManager.messages.SENT_HOME);
    }

    public static void deleteHome(String player, String home) {
        GSPlayer p = PlayerManager.getPlayer(player);
        Home h = getHome(p, home);

        if (h == null) {
            p.sendMessage(ConfigManager.messages.HOME_DOES_NOT_EXIST);
            return;
        }

        for (ArrayList<Home> list : p.getHomes().values()) {
            if (list.contains(h)) {
                list.remove(h);
                break;
            }
        }

        DatabaseManager.homes.deleteHome(h);

        p.sendMessage(ConfigManager.messages.HOME_DELETED);
    }
}

