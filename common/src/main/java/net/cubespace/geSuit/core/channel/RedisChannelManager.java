package net.cubespace.geSuit.core.channel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import redis.clients.jedis.BinaryJedisPubSub;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RedisChannelManager implements ChannelManager {
    private Set<Channel<?>> channels;
    private Map<String, Channel<?>> channelNameMap;
    private PubSubHandler channelHandler;
    private RedisConnection connection;

    public RedisChannelManager(RedisConnection connection, Logger logger) {
        this.connection = connection;
        
        channels = Sets.newHashSet();
        channelNameMap = Maps.newHashMap();
    }

    public Channel<byte[]> createChannel(String name) throws IllegalArgumentException {
        return createChannel(name, byte[].class);
    }

    public <T> Channel<T> createChannel(String name, Class<T> clazz) throws IllegalArgumentException {
        name = name.toLowerCase();
        Preconditions.checkArgument(getChannel(name) == null, "Channel " + name + " already exists");

        RedisChannel<T> channel = new RedisChannel<T>(name, clazz, connection);

        channels.add(channel);
        channelNameMap.put(name, channel);

        return channel;
    }

    public Channel<?> getChannel(String name) {
        return channelNameMap.get(name.toLowerCase());
    }

    public Collection<Channel<?>> getChannels() {
        return Collections.unmodifiableSet(channels);
    }

    public RedisConnection getRedis() {
        return connection;
    }

    /**
     * NOTE: This must be called on a thread. It will block until this is closed
     * 
     * @param latch This is used to wait until the subscription is active
     */
    public void initialize(CountDownLatch latch) {
        channelHandler = new PubSubHandler(latch);
        connection.subscribe("gesuit.*".getBytes(Charsets.UTF_8), channelHandler);
    }

    public void shutdown() {
        channelHandler.punsubscribe();
    }

    public class PubSubHandler extends BinaryJedisPubSub {
        private CountDownLatch mLatch;

        public PubSubHandler(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
        }

        @Override
        public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
            ByteArrayInputStream stream = new ByteArrayInputStream(message);
            DataInputStream in = new DataInputStream(stream);

            try {
                long key = in.readLong();

                if (key != connection.getKey()) {
                    byte[] data = Arrays.copyOfRange(message, 4, message.length);
                    String name = new String(channel, Charsets.UTF_8);
                    name = name.substring(4);

                    Channel<?> ch = getChannel(name);
                    if (ch != null)
                        ((RedisChannel<?>) ch).onReceive(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
            mLatch.countDown();
        }

    }
}
