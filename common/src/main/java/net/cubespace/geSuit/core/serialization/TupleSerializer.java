package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.objects.Tuple;

class TupleSerializer<A,B> extends AdvancedSerializer<Tuple<A,B>> {
    public TupleSerializer(TypeToken<Tuple<A, B>> type) {
        super(type);
    }
    
    @SuppressWarnings("unchecked")
    private TypeToken<A> getAType() {
        ParameterizedType type = (ParameterizedType)getType().getType();
        return (TypeToken<A>)TypeToken.of(type.getActualTypeArguments()[0]);
    }
    
    @SuppressWarnings("unchecked")
    private TypeToken<B> getBType() {
        ParameterizedType type = (ParameterizedType)getType().getType();
        return (TypeToken<B>)TypeToken.of(type.getActualTypeArguments()[1]);
    }

    @Override
    public boolean isSerializable() {
        return Serialization.isSerializable(getAType()) && Serialization.isSerializable(getBType());
    }

    @Override
    public void serialize(Tuple<A, B> object, DataOutput out) throws IOException {
        Serialization.serialize(object.getA(), getAType(), out);
        Serialization.serialize(object.getB(), getBType(), out);
    }

    @Override
    public Tuple<A, B> deserialize(DataInput in) throws IOException {
        A a = Serialization.deserialize(getAType(), in);
        B b = Serialization.deserialize(getBType(), in);
        
        return new Tuple<A, B>(a, b);
    }
}
