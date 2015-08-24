package net.cubespace.geSuit.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.GlobalServer;

public class Utilities {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static long parseDate(String date) {
        try {
            Date parsed = dateFormat.parse(date);
            return parsed.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Input date does not conform to the required format");
        }
    }
    
    public static String formatDate(long date) {
        return dateFormat.format(new Date(date));
    }
    
    public static UUID makeUUID(String uuid) {
        if (uuid.length() < 32) {
            throw new IllegalArgumentException("This is not a UUID");
        }
        if (!uuid.contains("-")) {
            return UUID.fromString(String.format("%s-%s-%s-%s-%s", uuid.substring(0, 8), uuid.substring(8, 12), uuid.substring(12, 16), uuid.substring(16, 20), uuid.substring(20)));
        } else {
            return UUID.fromString(uuid);
        }
    }
    
    public static String toString(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
    
    public static InetAddress makeInetAddress(String address) {
        // Rough match to filter out anything that isnt an IPv4 or IPv6 address
        if (address.matches("(?:\\d+.\\d+.\\d+.\\d+)|(?:[0-9a-fA-F]+(?::[0-9a-fA-F]*)+)")) {
            try {
                return InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Input is not an IP address");
            }
        } else {
            throw new IllegalArgumentException("Input is not an IP address");
        }
    }
    
    public static GlobalPlayer getPlayerAdvanced(String name) {
        GlobalPlayer player = Global.getOfflinePlayer(name);
        if (player == null) {
            player = Global.getPlayer(name);
        }
        
        return player;
    }
    
    public static Class<?> getComponentType(Collection<?> collection) {
        if (collection.isEmpty()) {
            return null;
        }
        
        Class<?> type = null;
        for (Object value : collection) {
            if (value == null) {
                continue;
            }
            
            if (type == null) {
                type = value.getClass();
            } else {
                Class<?> theirs = value.getClass();
                if (!type.equals(theirs) && !type.isAssignableFrom(theirs)) {
                    // See if their class is a super class
                    if (theirs.isAssignableFrom(type)) {
                        type = theirs;
                    } else {
                        // Not going to check for a common superclass/interface
                        type = Object.class;
                        break;
                    }
                }
            }
        }
        
        return type;
    }
    
    public static String colorize(String input) {
        input = input.replace("{N}", "\n");
        char[] b = input.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1) {
                b[i] = '\u00A7';
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }
        }
        return new String(b);
    }
    
    public static Set<String> matchPlayerNames(String partial, boolean useNicknames) {
        Set<String> names = Sets.newHashSet();
        partial = partial.toLowerCase();
        
        for (GlobalPlayer player : Global.getPlayers()) {
            if (player.getName().toLowerCase().startsWith(partial)) {
                names.add(player.getName());
            }
            
            if (useNicknames && player.hasNickname()) {
                if (player.getNickname().toLowerCase().startsWith(partial)) {
                    names.add(player.getNickname());
                }
            }
        }
        
        return names;
    }
    
    public static Set<String> matchServerNames(String partial) {
        Set<String> names = Sets.newHashSet();
        partial = partial.toLowerCase();
        
        for (GlobalServer server : Global.getServers()) {
            if (server.getName().toLowerCase().startsWith(partial)) {
                names.add(server.getName());
            }
        }
        
        return names;
    }
    
    /**
     * Selects the first value that isnt null. If both values are null, null is returned
     * @param a Value a
     * @param b Value b
     * @return Either a or b or null
     */
    public static <T> T selectFirst(T a, T b) {
        if (a != null) {
            return a;
        } else {
            return b;
        }
    }
}
