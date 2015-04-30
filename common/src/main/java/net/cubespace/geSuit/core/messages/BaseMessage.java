package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.cubespace.geSuit.core.channel.ChannelCodec;

import com.google.common.collect.HashBiMap;

public abstract class BaseMessage {

    public abstract void write(DataOutput out) throws IOException;

    public abstract void read(DataInput in) throws IOException;

    public static void write(BaseMessage message, DataOutput out) throws IOException {
        Integer id = mMessages.inverse().get(message.getClass());

        if (id == null) {
            System.err.println("Attempted to write unknown packet " + message.getClass().getName());
            return;
        }

        out.writeByte(id);
        message.write(out);
    }

    public static BaseMessage readMessage(DataInput in) throws IOException {
        int id = 0;
        try {
            id = in.readUnsignedByte();
            Class<? extends BaseMessage> clazz = mMessages.get(id);
            if (clazz == null) {
                System.err.println("Tried to read unknown packet " + id);
                return null;
            }

            BaseMessage message = clazz.newInstance();
            message.read(in);
            return message;
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("Error reading packet " + id);
            e.printStackTrace();
            return null;
        }
    }

    private static HashBiMap<Integer, Class<? extends BaseMessage>> mMessages = HashBiMap.create();

    private static void addMessageType(int id, Class<? extends BaseMessage> type) {
        mMessages.put(id, type);
    }

    static {
        addMessageType(0, PlayerUpdateMessage.class);
        addMessageType(1, PlayerUpdateRequestMessage.class);
        addMessageType(2, RemoteInvokeMessage.class);
        addMessageType(3, FireBanEventMessage.class);
        addMessageType(4, FireWarnEventMessage.class);
    }
    
    public static class Codec implements ChannelCodec<BaseMessage> {
        @Override
        public void encode(BaseMessage value, DataOutput out) throws IOException {
            write(value, out);
        }

        @Override
        public BaseMessage decode(DataInput in) throws IOException {
            return readMessage(in);
        }
    }
}
