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

/**
 * This serialization system creates minimal versions of data. 
 * The produced output has no type information present, instead
 * prior knowledge of the precise types is used.
 * 
 * <p>The type information used by this system includes type 
 * parameters such as {@code List<String>}. This allows the
 * system to not have to write type information simply because
 * both sides know the exact type to read. The result is as
 * minimal as doing it manually, but with the benefit of
 * being able to dynamically serialize data.</p>
 * 
 * <h2>Drawbacks</h2>
 * <p>This system is not able to serialize values of type 
 * {@code Object} or other unknown types such as collections 
 * with an unknown type parameter or wildcard parameters.</p>
 * 
 * @see TypeToken
 */
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
    
    /**
     * Serializes an object of {@code detailedType} to {@code out}
     * @param value The value to serialize
     * @param detailedType The exact type of {@code value}
     * @param out The output to write to
     * @throws IOException Thrown if an exception occurs while writing to {@code out}
     */
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
    
    /**
     * Deserializes an object of {@code detailedType} from {@code in}
     * @param detailedType The exact type to deserialize
     * @param in The input to read from
     * @return The deserialized object
     * @throws IOException Thrown if an exception occurs while reading from {@code in}
     */
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
    
    /**
     * Checks if a type can be serialized by this system
     * @param detailedType The exact type to be serialized
     * @return True if it can
     */
    public static boolean isSerializable(TypeToken<?> detailedType) {
        AdvancedSerializer<?> serializer = getSerializer(detailedType);
        if (serializer == null) {
            return false;
        }
        
        return serializer.isSerializable();
    }
    
    /**
     * Gets a serializer that is capable of reading and writing that type
     * @param type The exact type you wish to serialize / deserialize
     * @return A serializer object or null
     */
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
    
    /**
     * Registeres a dynamic serializer. Dynamic serializers are for variable types. eg. {@code List<?>}, {@code Storable}, etc.
     * These serializers are created as needed based on the type.
     * @param base The base type that things must implement to invoke this serializer
     * @param type The class to instantiate when one needs to be made. <b>NOTE:</b> This class requires a public constructor that takes a {@code TypeToken<T>} as a parameter 
     */
    public static <T> void registerDynamic(TypeToken<T> base, Class<? extends AdvancedSerializer> type) {
        dynamicSerializers.put(base, type);
    }
    
    /**
     * Registers a static serializer. Static serializers are for fixed types, eg. {@code Integer}, {@code String}, etc.
     * Only one instance of these serializers are used for all.
     * 
     * @param fixedSerializer The actual serializer object
     */
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
