package net.cubespace.geSuit.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
}