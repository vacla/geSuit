package net.cubespace.geSuit.core.storage;

import java.net.InetAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.util.Utilities;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Primitives;
import com.google.common.primitives.Shorts;

/**
 * The DataConversion system provides converters between string and various types.
 */
public class DataConversion {
    private static Map<Class<?>, Converter<String, ?>> registeredConverters;
    
    static {
        registeredConverters = Maps.newIdentityHashMap();
        
        // Basic converters
        registeredConverters.put(Double.class, Doubles.stringConverter());
        registeredConverters.put(Float.class, Floats.stringConverter());
        registeredConverters.put(Integer.class, Ints.stringConverter());
        registeredConverters.put(Short.class, Shorts.stringConverter());
        registeredConverters.put(Long.class, Longs.stringConverter());
        registeredConverters.put(String.class, Converter.<String>identity());
        registeredConverters.put(Boolean.class, new Converter<String, Boolean>() {
            @Override
            protected String doBackward(Boolean b) {
                return String.valueOf(b);
            }
            
            @Override
            protected Boolean doForward(String a) {
                return parseBoolean(a);
            }
        });
        
        // Extra converters
        registeredConverters.put(UUID.class, new Converter<String, UUID>() {
            @Override
            protected UUID doForward(String a) {
                return Utilities.makeUUID(a);
            }
            
            @Override
            protected String doBackward(UUID b) {
                return Utilities.toString(b);
            }
        });
        registeredConverters.put(InetAddress.class, new Converter<String, InetAddress>() {
            @Override
            protected InetAddress doForward(String a) {
                return InetAddresses.forString(a);
            }
            
            @Override
            protected String doBackward(InetAddress b) {
                return b.getHostAddress();
            }
        });
        registeredConverters.put(DateDiff.class, new Converter<String, DateDiff>() {
            @Override
            protected DateDiff doForward(String a) {
                return DateDiff.valueOf(a);
            }
            
            @Override
            protected String doBackward(DateDiff b) {
                return b.toString();
            }
        });
    }
    
    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        
        Converter<String, ?> converter = getConverter(object.getClass());
        if (converter != null) {
            return ((Converter<String, Object>)converter).reverse().convert(object);
        } else {
            return null;
        }
    }
    
    public static <T> T fromString(String string, Class<T> type) {
        if (string == null) {
            return null;
        }
        
        Converter<String, ?> converter = getConverter(type);
        if (converter != null) {
            return (T)converter.convert(string);
        } else {
            return null;
        }
    }
    
    /**
     * Checks if a converter is available for the specified type
     * @param type The type wanted
     * @return True if one is available
     */
    public static boolean isConvertable(Class<?> type) {
        return registeredConverters.containsKey(type);
    }
    
    /**
     * Attempts to get a converter for {@code type}.
     * @param type The type to get
     * @return A converter that can convert between {@code type} and {@code String} or null if none is available
     */
    public static <A> Converter<String, A> getConverter(final Class<A> type) {
        if (SimpleStorable.class.isAssignableFrom(type)) {
            return new Converter<String, A>() {
                @Override
                protected A doForward(String a) {
                    try {
                        A storable = type.newInstance();
                        ((SimpleStorable)storable).load(a);
                        return storable;
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    }
                    return null;
                }
                
                @Override
                protected String doBackward(A b) {
                    return ((SimpleStorable)b).save();
                }
            };
        } else if (Enum.class.isAssignableFrom(type)) {
            return (Converter<String, A>)getEnumConverter((Class<Enum>)type);
        } else if (type.isPrimitive()) {
            return (Converter<String, A>)registeredConverters.get(Primitives.wrap(type));
        } else {
            return (Converter<String, A>)registeredConverters.get(type);
        }
    }
    
    public static <E extends Enum<E>> Converter<String, E> getEnumConverter(final Class<E> enumType) {
        return new Converter<String, E>() {
            @Override
            protected String doBackward(E b) {
                return b.name();
            }
            
            @Override
            protected E doForward(String a) {
                for (E value : EnumSet.allOf(enumType)) {
                    if (value.name().equalsIgnoreCase(a)) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("Unknown value " + a + " for " + enumType.getSimpleName());
            }
        };
    }
    
    /**
     * Attempts to convert a string list to a typed list using the registered converters
     * @param source The string list to convert
     * @param type The type you want out
     * @return A converted list
     * @throws IllegalArgumentException Thrown if no converter is available for {@code type}
     */
    public static <T> List<T> convertList(List<String> source, Class<T> type) {
        List<T> target = Lists.newArrayListWithCapacity(source.size());
        
        Converter<String, T> converter = getConverter(type);
        if (converter == null) {
            throw new IllegalArgumentException("Unable to convert to " + type.getSimpleName());
        }
        
        for (String val : source) {
            target.add(converter.convert(val));
        }
        
        return target;
    }
    
    /**
     * Attempts to convert a typed list to a string list using the registered converters
     * @param source The typed list to convert
     * @return A converted string list
     * @throws IllegalArgumentException Thrown if no converter is available for the list type
     */
    public static List<String> reverseConvertList(List<Object> source) {
        List<String> target = Lists.newArrayListWithCapacity(source.size());
        
        Class<?> type = Utilities.getComponentType(source);
        Converter<String, Object> converter = (Converter<String, Object>)getConverter(type);
        
        if (converter == null) {
            throw new IllegalArgumentException("Unable to convert to " + type.getSimpleName());
        }
        
        for (Object val : source) {
            target.add(converter.reverse().convert(val));
        }
        
        return target;
    }
    
    /**
     * Attempts to convert a string set to a typed set using the registered converters
     * @param source The string set to convert
     * @param type The type you want out
     * @return A converted set
     * @throws IllegalArgumentException Thrown if no converter is available for {@code type}
     */
    public static <T> Set<T> convertSet(Set<String> source, Class<T> type) {
        Set<T> target = Sets.newHashSetWithExpectedSize(source.size());
        
        Converter<String, T> converter = getConverter(type);
        if (converter == null) {
            throw new IllegalArgumentException("Unable to convert to " + type.getSimpleName());
        }
        
        for (String val : source) {
            target.add(converter.convert(val));
        }
        
        return target;
    }
    
    /**
     * Attempts to convert a typed set to a string set using the registered converters
     * @param source The typed set to convert
     * @return A converted string set
     * @throws IllegalArgumentException Thrown if no converter is available for the set type
     */
    public static <T> Set<String> reverseConvertSet(Set<Object> source) {
        Set<String> target = Sets.newHashSetWithExpectedSize(source.size());
        
        Class<?> type = Utilities.getComponentType(source);
        Converter<String, Object> converter = (Converter<String, Object>)getConverter(type);
        
        if (converter == null) {
            throw new IllegalArgumentException("Unable to convert to " + type.getSimpleName());
        }
        
        for (Object val : source) {
            target.add(converter.reverse().convert(val));
        }
        
        return target;
    }
    
    /**
     * A better boolean parser than Boolean.parseBoolean(). It supports parsing numbers as booleans 
     * and will throw an exception if the input is not a boolean
     * @param input The input string to parse
     * @return the parsed boolean
     * @throws IllegalArgumentException Thrown if the input cannot be parsed as a boolean
     */
    public static boolean parseBoolean(String input) {
        try {
            if (input.equalsIgnoreCase("true")) {
                return true;
            } else if (input.equalsIgnoreCase("false")) {
                return false;
            } else {
                int num = Integer.parseInt(input);
                return num != 0;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert " + input + " to a boolean");
        }
    }
}
