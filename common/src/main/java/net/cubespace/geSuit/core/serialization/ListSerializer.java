package net.cubespace.geSuit.core.serialization;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

class ListSerializer<T> extends CollectionSerializer<T, List<T>> {
    public ListSerializer(TypeToken<List<T>> type) {
        super(type);
    }
    
    @Override
    protected List<T> makeCollection(int size) {
        return Lists.newArrayListWithCapacity(size);
    }
}
