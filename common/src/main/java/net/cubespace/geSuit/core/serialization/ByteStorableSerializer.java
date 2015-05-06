package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.storage.ByteStorable;

class ByteStorableSerializer<E extends ByteStorable> extends AdvancedSerializer<E> {
    private Invokable<E, E> constructor;
    
    public ByteStorableSerializer(TypeToken<E> type) {
        super(type);
        
        try {
            constructor = type.constructor(type.getRawType().getDeclaredConstructor());
            constructor.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
        }
    }

    @Override
    public boolean isSerializable() {
        return constructor != null;
    }

    @Override
    public void serialize(E object, DataOutput out) throws IOException {
        object.save(out);
    }

    @Override
    public E deserialize(DataInput in) throws IOException {
        try {
            E object = constructor.invoke(null);
            object.load(in);
            return object;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

}
