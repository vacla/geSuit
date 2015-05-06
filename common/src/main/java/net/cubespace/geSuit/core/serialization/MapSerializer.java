package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("unchecked")
class MapSerializer<K, V> extends AdvancedSerializer<Map<K,V>> {
    public MapSerializer(TypeToken<Map<K, V>> type) {
        super(type);
    }

    private TypeToken<K> getKeyType() {
        ParameterizedType type = (ParameterizedType)getType().getType();
        return (TypeToken<K>)TypeToken.of(type.getActualTypeArguments()[0]);
    }
    
    private TypeToken<V> getValueType() {
        ParameterizedType type = (ParameterizedType)getType().getType();
        return (TypeToken<V>)TypeToken.of(type.getActualTypeArguments()[1]);
    }
    
    @Override
    public boolean isSerializable() {
        return Serialization.isSerializable(getKeyType()) && Serialization.isSerializable(getValueType());
    }
    
    @Override
    public void serialize(Map<K, V> object, DataOutput out) throws IOException {
        TypeToken<K> keyType = getKeyType();
        TypeToken<V> valueType = getValueType();

        // Write the map contents
        out.writeInt(object.size());
        for (Entry<K, V> entry : object.entrySet()) {
            Serialization.serialize(entry.getKey(), keyType, out);
            Serialization.serialize(entry.getValue(), valueType, out);
        }
    }
    
    @Override
    public Map<K, V> deserialize(DataInput in) throws IOException {
        TypeToken<K> keyType = getKeyType();
        TypeToken<V> valueType = getValueType();
        
        int size = in.readInt();
        Map<K, V> map = Maps.newLinkedHashMap();
        
        // Read the map contents
        for (int i = 0; i < size; ++i) {
            map.put(Serialization.deserialize(keyType, in), Serialization.deserialize(valueType, in));
        }
        
        return map;
    }
}
