package net.cubespace.geSuit.core.storage;

import java.net.InetAddress;
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
import com.google.common.primitives.Shorts;

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
    
    public static boolean isConvertable(Class<?> type) {
        return registeredConverters.containsKey(type);
    }
    
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
        } else {
            return (Converter<String, A>)registeredConverters.get(type);
        }
    }
    
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
