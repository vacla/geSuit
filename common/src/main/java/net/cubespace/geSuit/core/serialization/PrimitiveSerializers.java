package net.cubespace.geSuit.core.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.reflect.TypeToken;

final class PrimitiveSerializers {
    public static class IntegerSerializer extends AdvancedSerializer<Integer> {
        protected IntegerSerializer() {
            super(TypeToken.of(Integer.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Integer object, DataOutput out) throws IOException {
            out.writeInt(object);
        }

        @Override
        public Integer deserialize(DataInput in) throws IOException {
            return in.readInt();
        }
    }
    
    public static class ShortSerializer extends AdvancedSerializer<Short> {
        protected ShortSerializer() {
            super(TypeToken.of(Short.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Short object, DataOutput out) throws IOException {
            out.writeShort(object);
        }

        @Override
        public Short deserialize(DataInput in) throws IOException {
            return in.readShort();
        }
    }
    
    public static class LongSerializer extends AdvancedSerializer<Long> {
        protected LongSerializer() {
            super(TypeToken.of(Long.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Long object, DataOutput out) throws IOException {
            out.writeLong(object);
        }

        @Override
        public Long deserialize(DataInput in) throws IOException {
            return in.readLong();
        }
    }
    
    public static class ByteSerializer extends AdvancedSerializer<Byte> {
        protected ByteSerializer() {
            super(TypeToken.of(Byte.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Byte object, DataOutput out) throws IOException {
            out.writeByte(object);
        }

        @Override
        public Byte deserialize(DataInput in) throws IOException {
            return in.readByte();
        }
    }
    
    public static class FloatSerializer extends AdvancedSerializer<Float> {
        protected FloatSerializer() {
            super(TypeToken.of(Float.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Float object, DataOutput out) throws IOException {
            out.writeFloat(object);
        }

        @Override
        public Float deserialize(DataInput in) throws IOException {
            return in.readFloat();
        }
    }
    
    public static class DoubleSerializer extends AdvancedSerializer<Double> {
        protected DoubleSerializer() {
            super(TypeToken.of(Double.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Double object, DataOutput out) throws IOException {
            out.writeDouble(object);
        }

        @Override
        public Double deserialize(DataInput in) throws IOException {
            return in.readDouble();
        }
    }
    
    public static class BooleanSerializer extends AdvancedSerializer<Boolean> {
        protected BooleanSerializer() {
            super(TypeToken.of(Boolean.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Boolean object, DataOutput out) throws IOException {
            out.writeBoolean(object);
        }

        @Override
        public Boolean deserialize(DataInput in) throws IOException {
            return in.readBoolean();
        }
    }
    
    public static class CharSerializer extends AdvancedSerializer<Character> {
        protected CharSerializer() {
            super(TypeToken.of(Character.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(Character object, DataOutput out) throws IOException {
            out.writeChar(object);
        }

        @Override
        public Character deserialize(DataInput in) throws IOException {
            return in.readChar();
        }
    }
    
    public static class StringSerializer extends AdvancedSerializer<String> {
        protected StringSerializer() {
            super(TypeToken.of(String.class));
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public void serialize(String object, DataOutput out) throws IOException {
            out.writeUTF(object);
        }

        @Override
        public String deserialize(DataInput in) throws IOException {
            return in.readUTF();
        }
    }
}
