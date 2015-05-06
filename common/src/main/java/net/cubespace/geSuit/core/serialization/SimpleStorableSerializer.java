package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.storage.SimpleStorable;

class SimpleStorableSerializer<E extends SimpleStorable> extends AdvancedSerializer<E> {
    private Invokable<E, E> constructor;
    
    public SimpleStorableSerializer(TypeToken<E> type) {
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
        out.writeUTF(object.save());
    }

    @Override
    public E deserialize(DataInput in) throws IOException {
        try {
            E object = constructor.invoke(null);
            object.load(in.readUTF());
            return object;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

}
