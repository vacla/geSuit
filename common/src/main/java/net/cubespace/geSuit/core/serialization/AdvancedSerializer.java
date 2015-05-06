package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.reflect.TypeToken;

public abstract class AdvancedSerializer<T> {
    private TypeToken<T> type;
    
    protected AdvancedSerializer(TypeToken<T> type) {
        this.type = type;
    }
    
    public TypeToken<T> getType() {
        return type;
    }
    
    public abstract boolean isSerializable();
    
    public abstract void serialize(T object, DataOutput out) throws IOException;
    
    public abstract T deserialize(DataInput in) throws IOException;
}
