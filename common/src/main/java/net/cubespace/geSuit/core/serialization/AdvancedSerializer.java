package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.reflect.TypeToken;

/**
 * This is the base class for any serialize written for
 * the {@link Serialization} system.
 * 
 * If this will be a static serializer (one based on an exact type such as {@code Integer}),
 * you can have any constructor you want.
 * If this will be a dynamic serializer (one based on variable types such as {@code List<>}),
 * you must have a public constructor taking a {@code TypeToken<T>}
 * 
 * @param <T> This should be the type this serializer can serialize. Example: Integer for an int serializer
 */
public abstract class AdvancedSerializer<T> {
    private TypeToken<T> type;
    
    protected AdvancedSerializer(TypeToken<T> type) {
        this.type = type;
    }
    
    /**
     * @return Returns the exact type this serialize can serialize
     */
    public TypeToken<T> getType() {
        return type;
    }
    
    /**
     * @return Returns true if the type is serializable. This should always be true for static serializers.
     *         Dynamic serializers might do a check with {@link Serialization#isSerializable(TypeToken)}
     *         on type parameters.
     */
    public abstract boolean isSerializable();
    
    /**
     * Turns the object into bytes
     * @param object The value to serialize
     * @param out The destination
     * @throws IOException Thrown if an error occurs writing
     */
    public abstract void serialize(T object, DataOutput out) throws IOException;
    
    /**
     * Reads the object from bytes
     * @param in The source to read from
     * @return The value
     * @throws IOException Thrown if an error occurs reading
     */
    public abstract T deserialize(DataInput in) throws IOException;
}
