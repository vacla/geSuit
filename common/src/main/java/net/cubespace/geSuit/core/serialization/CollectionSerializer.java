package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("unchecked")
abstract class CollectionSerializer<E, T extends Collection<E>> extends AdvancedSerializer<T> {
    public CollectionSerializer(TypeToken<T> type) {
        super(type);
    }

    private TypeToken<E> getComponentType() {
        ParameterizedType type = (ParameterizedType)getType().getType();
        return (TypeToken<E>)TypeToken.of(type.getActualTypeArguments()[0]);
    }
    
    @Override
    public boolean isSerializable() {
        TypeToken<E> type = getComponentType();
        return Serialization.isSerializable(type);
    }
    
    @Override
    public void serialize(T object, DataOutput out) throws IOException {
        TypeToken<E> type = getComponentType();

        // Write the list contents
        out.writeInt(object.size());
        for (E value : object) {
            Serialization.serialize(value, type, out);
        }
    }

    protected abstract T makeCollection(int size);
    
    @Override
    public T deserialize(DataInput in) throws IOException {
        TypeToken<E> type = getComponentType();
        
        int size = in.readInt();
        T collection = makeCollection(size);
        
        // Read the list contents
        for (int i = 0; i < size; ++i) {
            collection.add(Serialization.deserialize(type, in));
        }
        
        return collection;
    }
}
