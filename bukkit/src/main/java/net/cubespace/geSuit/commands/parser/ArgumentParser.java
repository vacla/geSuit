package net.cubespace.geSuit.commands.parser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.objects.DateDiff;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

public class ArgumentParser {
    private static Map<Class<?>, Function<String, ?>> converters = Maps.newHashMap();
    
    @SuppressWarnings("unchecked")
    public static <T> Function<String,T> getParser(Class<T> type) {
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }
        
        // Integer special case
        if (type == Integer.class) {
            return (Function<String, T>)convertInteger(false, 10);
        } else if (type == Short.class) {
            return (Function<String, T>)convertShort(false, 10);
        } else if (type == Long.class) {
            return (Function<String, T>)convertLong(false, 10);
        }
        
        // General case
        if (converters.containsKey(type)) {
            return (Function<String, T>)converters.get(type);
        }
        
        return null;
    }
    
    public static <T> void registerParser(Class<T> targetType, Function<String, T> converter) {
        converters.put(targetType, converter);
    }
    
    public static final Function<String, String> CONVERT_STRING = new Function<String, String>() {
        @Override
        public String apply(String text) {
            return text;
        }
    };
    
    public static Function<String, Long> convertLong(final boolean unsigned, final int radix) {
        return new Function<String, Long>() {
            @Override
            public Long apply(String text) {
                Long value = Long.valueOf(text, radix);
                
                if (unsigned && value < 0)
                    throw new IllegalArgumentException("Value cannot be negative");
                
                return value;
            }
        };
    }
    
    public static Function<String, Integer> convertInteger(final boolean unsigned, final int radix) {
        return new Function<String, Integer>() {
            @Override
            public Integer apply(String text) {
                Integer value = Integer.valueOf(text, radix);
                
                if (unsigned && value < 0)
                    throw new IllegalArgumentException("Value cannot be negative");
                
                return value;
            }
        };
    }
    
    public static Function<String, Short> convertShort(final boolean unsigned, final int radix) {
        return new Function<String, Short>() {
            @Override
            public Short apply(String text) {
                Short value = Short.valueOf(text, radix);
                
                if (unsigned && value < 0)
                    throw new IllegalArgumentException("Value cannot be negative");
                
                return value;
            }
        };
    }
    
    public static final Function<String, Float> CONVERT_FLOAT = new Function<String, Float>() {
        @Override
        public Float apply(String text) {
            return Float.parseFloat(text);
        }
    };
    
    public static final Function<String, Double> CONVERT_DOUBLE = new Function<String, Double>() {
        @Override
        public Double apply(String text) {
            return Double.parseDouble(text);
        }
    };
    
    public static final Function<String, Boolean> CONVERT_BOOLEAN = new Function<String, Boolean>() {
        @Override
        public Boolean apply(String text) {
            if (text.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            } else if (text.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            } else {
                throw new IllegalArgumentException("Expected true or false");
            }
        }
    };
    
    public static final Function<String, Player> CONVERT_PLAYER = new Function<String, Player>() {
        @Override
        public Player apply(String text) {
            Player player = Bukkit.getPlayer(text);
            if (player == null) {
                throw new IllegalArgumentException("Unknown player " + text);
            }
            return player;
        }
    };
    
    public static final Function<String, OfflinePlayer> CONVERT_OFFLINEPLAYER = new Function<String, OfflinePlayer>() {
        @Override
        public OfflinePlayer apply(String text) {
            return Bukkit.getOfflinePlayer(text);
        }
    };
    
    public static final Function<String, InetAddress> CONVERT_INETADDRESS = new Function<String, InetAddress>() {
        @Override
        public InetAddress apply(String text) {
            try {
                return InetAddress.getByName(text);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    };
    
    public static final Function<String, UUID> CONVERT_UUID = new Function<String, UUID>() {
        @Override
        public UUID apply(String text) {
            return UUID.fromString(text);
        }
    };
    
    public static final Function<String, DateDiff> CONVERT_DATEDIFF = new Function<String, DateDiff>() {
        @Override
        public DateDiff apply(String text) {
            return DateDiff.valueOf(text);
        }
    };
    
    static {
        converters.put(String.class, CONVERT_STRING);
        converters.put(Double.class, CONVERT_DOUBLE);
        converters.put(Float.class, CONVERT_FLOAT);
        converters.put(Boolean.class, CONVERT_BOOLEAN);
        converters.put(Player.class, CONVERT_PLAYER);
        converters.put(OfflinePlayer.class, CONVERT_OFFLINEPLAYER);
        converters.put(InetAddress.class, CONVERT_INETADDRESS);
        converters.put(UUID.class, CONVERT_UUID);
        converters.put(DateDiff.class, CONVERT_DATEDIFF);
    }
}
