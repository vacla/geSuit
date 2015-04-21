package net.cubespace.geSuit.core.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import net.cubespace.geSuit.core.storage.RedisConnection;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

class RedisChannel<T> implements Channel<T> {
    private String name;
    private byte[] byteName;
    private Class<T> channelClass;
    private ChannelCodec<T> channelCodec;
    private RedisConnection connection;

    private LinkedList<ChannelDataReceiver<T>> receivers;

    public RedisChannel(String name, Class<T> type, RedisConnection connection) {
        this.name = name;
        this.channelClass = type;
        this.connection = connection;

        byteName = String.format("gesuit.%s", name).getBytes(Charsets.UTF_8);

        receivers = new LinkedList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public byte[] getByteName() {
        return byteName;
    }

    @Override
    public void setCodec(ChannelCodec<T> codec) {
        Preconditions.checkNotNull(codec);
        channelCodec = codec;
    }

    private byte[] encodeValue(T value) {
        if (channelClass.equals(byte[].class))
            return (byte[]) value;

        Preconditions.checkNotNull(channelCodec, "No codec has been set. Unable to translate value for sending");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            channelCodec.encode(value, out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Encoding error: ", e);
        }

        return stream.toByteArray();
    }

    @Override
    public void broadcast(T value) {
        Preconditions.checkNotNull(value);

        byte[] data = encodeValue(value);

        ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length + 4);
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeLong(connection.getKey()); // Prevents this server from receiving its own messages
            out.write(data);
        } catch (IOException e) { // Cant happen
        }

        connection.publish(byteName, stream.toByteArray());
    }

    @Override
    public void addReceiver(ChannelDataReceiver<T> receiver) {
        synchronized (receivers) {
            if (!receivers.contains(receiver))
                receivers.add(receiver);
        }
    }

    @SuppressWarnings("unchecked")
    private T decodeValue(byte[] data) {
        if (channelClass.equals(byte[].class))
            return (T) data;

        if (channelCodec == null) {
            System.err.println("[geSuit] No codec has been set. Unable to translate value for receiving (" + getName() + ")");
            return null;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(stream);

        try {
            return channelCodec.decode(in);
        } catch (IOException e) {
            System.err.println("[geSuit] An error occured while decoding a channel message (" + getName() + ")");
            e.printStackTrace();
            return null;
        }
    }

    public void onReceive(byte[] data) {
        T value = decodeValue(data);
        if (value == null)
            return;

        synchronized (receivers) {
            for (ChannelDataReceiver<T> receiver : receivers) {
                try {
                    receiver.onDataReceive(this, value);
                } catch (Throwable e) {
                    System.err.println("[geSuit] An error occured while passing a channel message (" + getName() + ") to the receiver " + receiver.getClass().getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
