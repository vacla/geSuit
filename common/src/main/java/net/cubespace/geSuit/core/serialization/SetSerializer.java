package net.cubespace.geSuit.core.serialization;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

class SetSerializer<T> extends CollectionSerializer<T, Set<T>> {
    public SetSerializer(TypeToken<Set<T>> type) {
        super(type);
    }
    
    @Override
    protected Set<T> makeCollection(int size) {
        return Sets.newHashSetWithExpectedSize(size);
    }
}
