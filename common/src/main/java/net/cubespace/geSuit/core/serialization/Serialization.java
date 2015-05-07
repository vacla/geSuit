package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.storage.SimpleStorable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Serialization {
    private static final Map<TypeToken<?>, AdvancedSerializer<?>> staticSerializers = Maps.newHashMap();
    private static final Map<TypeToken<?>, Class<? extends AdvancedSerializer>> dynamicSerializers = Maps.newHashMap();
    private static final Cache<TypeToken<?>, AdvancedSerializer<?>> cachedDynamicSerializers;
    
    static {
        cachedDynamicSerializers = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build();
    }
    
    public static <T> void serialize(T value, TypeToken<T> detailedType, DataOutput out) throws IOException {
        AdvancedSerializer<T> serializer = getSerializer(detailedType);
        if (serializer == null || !serializer.isSerializable()) {
            throw new IllegalArgumentException("The specified type is not serializable");
        }
        
        if (value == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            serializer.serialize(value, out);
        }
    }
    
    public static <T> T deserialize(TypeToken<T> detailedType, DataInput in) throws IOException {
        AdvancedSerializer<T> serializer = getSerializer(detailedType);
        if (serializer == null || !serializer.isSerializable()) {
            throw new IllegalArgumentException("The specified type is not serializable");
        }
        
        if (in.readBoolean()) {
            return serializer.deserialize(in);
        } else {
            return null;
        }
    }
    
    public static boolean isSerializable(TypeToken<?> detailedType) {
        AdvancedSerializer<?> serializer = getSerializer(detailedType);
        if (serializer == null) {
            return false;
        }
        
        return serializer.isSerializable();
    }
    
    public static <T> AdvancedSerializer<T> getSerializer(TypeToken<T> type) {
        if (type.isPrimitive()) {
            type = type.wrap();
        }
        
        // Try a static serializer
        if (staticSerializers.containsKey(type)) {
            return (AdvancedSerializer<T>)staticSerializers.get(type);
        }
        
        // See if it has been loaded before
        AdvancedSerializer<T> serializer = (AdvancedSerializer<T>)cachedDynamicSerializers.getIfPresent(type);
        if (serializer != null) {
            return serializer;
        }
        
        // See if any dynamic ones are possible
        Class<? extends AdvancedSerializer> serializerType = null;
        for (TypeToken<?> base : dynamicSerializers.keySet()) {
            if (base.isAssignableFrom(type)) {
                serializerType = dynamicSerializers.get(base);
                break;
            }
        }
        
        if (serializerType == null) {
            return null;
        }
        
        // Build a new one
        try {
            Invokable<? extends AdvancedSerializer<T>, ? extends AdvancedSerializer<T>> constructor = (Invokable<? extends AdvancedSerializer<T>, ? extends AdvancedSerializer<T>>) Invokable.from(serializerType.getConstructor(TypeToken.class));
            constructor.setAccessible(true);
            serializer = constructor.invoke(null, type);
            cachedDynamicSerializers.put(type, serializer);
            return serializer;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static <T> void registerDynamic(TypeToken<T> base, Class<? extends AdvancedSerializer> type) {
        dynamicSerializers.put(base, type);
    }
    
    public static void registerStatic(AdvancedSerializer<?> fixedSerializer) {
        staticSerializers.put(fixedSerializer.getType(), fixedSerializer);
    }
    
    static {
        registerStatic(new PrimitiveSerializers.ByteSerializer());
        registerStatic(new PrimitiveSerializers.ShortSerializer());
        registerStatic(new PrimitiveSerializers.IntegerSerializer());
        registerStatic(new PrimitiveSerializers.LongSerializer());
        registerStatic(new PrimitiveSerializers.FloatSerializer());
        registerStatic(new PrimitiveSerializers.DoubleSerializer());
        registerStatic(new PrimitiveSerializers.BooleanSerializer());
        registerStatic(new PrimitiveSerializers.CharSerializer());
        registerStatic(new PrimitiveSerializers.StringSerializer());
        registerStatic(new GlobalPlayerSerializer());
        registerStatic(new InetAddressSerializer());
        registerStatic(new UUIDSerializer());
        
        registerDynamic(TypeToken.of(List.class), ListSerializer.class);
        registerDynamic(TypeToken.of(Set.class), SetSerializer.class);
        registerDynamic(TypeToken.of(Map.class), MapSerializer.class);
        registerDynamic(TypeToken.of(ByteStorable.class), ByteStorableSerializer.class);
        registerDynamic(TypeToken.of(SimpleStorable.class), SimpleStorableSerializer.class);
    }
}
