package net.cubespace.geSuit.core.channel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.cubespace.geSuit.core.storage.RedisConnection;

import org.junit.Test;

import com.google.common.base.Charsets;

public class TestRedisChannel {

    @Test
    public void testChannelName() {
        RedisChannel<byte[]> channel = new RedisChannel<byte[]>("testname", byte[].class, null);
        
        assertArrayEquals("gesuit.testname".getBytes(Charsets.UTF_8), channel.getByteName());
    }
    
    @Test
    public void testByteSend() {
        RedisConnection fakeConnection = mock(FakeRedisConnection.class);
        
        RedisChannel<byte[]> channel = new RedisChannel<byte[]>("testname", byte[].class, fakeConnection);
        
        byte[] data = new byte[] {0, 100, 5, 3, 22, 121, 2, -5, -123, 55, 21, -4, 61, 99};
        byte[] sentDataBroadcast = new byte[] {0, 0, 0, 0, -1, -1, -1, -1, 0, 100, 5, 3, 22, 121, 2, -5, -123, 55, 21, -4, 61, 99};
        byte[] sentData = new byte[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 100, 5, 3, 22, 121, 2, -5, -123, 55, 21, -4, 61, 99};
        
        channel.broadcast(data);
        
        verify(fakeConnection, times(1)).publish(channel.getByteName(), sentDataBroadcast);
        
        channel.send(data, 1);
        
        verify(fakeConnection, times(1)).publish(channel.getByteName(), sentData);
    }
    
    @Test
    public void testEncode() {
        RedisConnection fakeConnection = mock(FakeRedisConnection.class);
        RedisChannel<int[]> channel = new RedisChannel<int[]>("testname", int[].class, fakeConnection);
        
        channel.setCodec(new IntegerCodec());
        
        int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        byte[] encodedData = new byte[] {0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 7};
        
        channel.broadcast(data);
        
        verify(fakeConnection, times(1)).publish(channel.getByteName(), encodedData);
    }
    
    private int[] decodeValue;
    @Test
    public void testDecode() {
        RedisChannel<int[]> channel = new RedisChannel<int[]>("testname", int[].class, null);
        
        channel.setCodec(new IntegerCodec());
        
        final int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        final byte[] encodedData = new byte[] {0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 7};
        
        
        channel.addReceiver(new ChannelDataReceiver<int[]>() {
            @Override
            public void onDataReceive(Channel<int[]> channel, int[] value, int sourceId, boolean isBroadcast) {
                decodeValue = value;
            }
        });
        
        channel.onReceive(encodedData, 0, false);
        assertArrayEquals(data, decodeValue);
    }
    
    private static class FakeRedisConnection extends RedisConnection {
        public FakeRedisConnection() {
            super("", 0, null, 0);
        }
    }
    
    private static class IntegerCodec implements ChannelCodec<int[]> {
        @Override
        public void encode(int[] value, DataOutput out) throws IOException {
            out.writeInt(value.length);
            for (int v : value) {
                out.writeInt(v);
            }
        }

        @Override
        public int[] decode(DataInput in) throws IOException {
            int size = in.readInt();
            int[] data = new int[size];
            for (int i = 0; i < size; ++i) {
                data[i] = in.readInt();
            }
            
            return data;
        }
    }
}
