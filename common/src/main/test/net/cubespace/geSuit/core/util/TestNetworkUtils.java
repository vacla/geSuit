package net.cubespace.geSuit.core.util;

import static org.junit.Assert.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.WarnAction.ActionType;
import net.cubespace.geSuit.core.storage.ByteStorable;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.net.InetAddresses;

public class TestNetworkUtils {
    @Test
    public void testCheckBasicSendibles() {
        assertTrue(NetworkUtils.isSendible(Byte.TYPE));
        assertTrue(NetworkUtils.isSendible(Byte.class));
        assertTrue(NetworkUtils.isSendible(Short.TYPE));
        assertTrue(NetworkUtils.isSendible(Short.class));
        assertTrue(NetworkUtils.isSendible(Integer.TYPE));
        assertTrue(NetworkUtils.isSendible(Integer.class));
        assertTrue(NetworkUtils.isSendible(Long.TYPE));
        assertTrue(NetworkUtils.isSendible(Long.class));
        assertTrue(NetworkUtils.isSendible(Float.TYPE));
        assertTrue(NetworkUtils.isSendible(Float.class));
        assertTrue(NetworkUtils.isSendible(Double.TYPE));
        assertTrue(NetworkUtils.isSendible(Double.class));
        assertTrue(NetworkUtils.isSendible(Boolean.TYPE));
        assertTrue(NetworkUtils.isSendible(Boolean.class));
        assertTrue(NetworkUtils.isSendible(Character.TYPE));
        assertTrue(NetworkUtils.isSendible(Character.class));
        assertTrue(NetworkUtils.isSendible(String.class));
        
        assertTrue(NetworkUtils.isSendible(UUID.class));
        assertTrue(NetworkUtils.isSendible(InetAddress.class));
        assertTrue(NetworkUtils.isSendible(GlobalPlayer.class));
        assertTrue(NetworkUtils.isSendible(ByteStorable.class));
        assertTrue(NetworkUtils.isSendible(Serializable.class));
        
        assertTrue(NetworkUtils.isSendible(List.class));
        assertTrue(NetworkUtils.isSendible(Set.class));
        assertTrue(NetworkUtils.isSendible(Map.class));
    }
    
    @Test
    public void testUUID() throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        UUID id = UUID.randomUUID();
        NetworkUtils.writeUUID(out, id);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        UUID result = NetworkUtils.readUUID(in);
        
        assertEquals(id, result);
    }
    
    @Test
    public void testInetAddress() throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        InetAddress address = InetAddresses.forString("127.0.0.1");
        NetworkUtils.writeInetAddress(out, address);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        InetAddress result = NetworkUtils.readInetAddress(in);
        
        assertEquals(address, result);
    }
    
    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        List<Integer> expected = Lists.newArrayList(1,2,3,4);
        NetworkUtils.writeObject(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        Object result = NetworkUtils.readObject(in);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testEnum() throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        ActionType expected = ActionType.TempIPBan;
        NetworkUtils.writeEnum(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        ActionType result = NetworkUtils.readEnum(in, ActionType.class);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testList() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        List<Integer> expected = Lists.newArrayList(1,2,3,4);
        NetworkUtils.writeList(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        List<?> result = NetworkUtils.readList(in);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testSet() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        Set<Integer> expected = Sets.newHashSet(1,2,3,4);
        NetworkUtils.writeSet(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        Set<?> result = NetworkUtils.readSet(in);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testMap() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        Map<String, Integer> expected = Maps.newHashMap();
        expected.put("key1", 1);
        expected.put("key2", 2);
        expected.put("key3", 3);
        expected.put("key4", 4);
        
        NetworkUtils.writeMap(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        Map<?, ?> result = NetworkUtils.readMap(in);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testByteSendible() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        TestByteStorable expected = new TestByteStorable(123451);
        
        NetworkUtils.writeByteStorable(out, expected);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        ByteStorable result = NetworkUtils.readByteStorable(in);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testTyped() throws IOException, ClassNotFoundException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        // Do the basics first
        NetworkUtils.writeTyped(out, (byte)5);
        NetworkUtils.writeTyped(out, (short)10);
        NetworkUtils.writeTyped(out, (int)123456);
        NetworkUtils.writeTyped(out, (long)89712398712L);
        NetworkUtils.writeTyped(out, (float)1.0f);
        NetworkUtils.writeTyped(out, (double)3.0);
        NetworkUtils.writeTyped(out, true);
        NetworkUtils.writeTyped(out, '4');
        NetworkUtils.writeTyped(out, "string");
        
        // Now do more complex stuff
        InetAddress address = InetAddresses.forString("127.0.0.1");
        UUID id = UUID.randomUUID();
        TestByteStorable storable = new TestByteStorable(123451);
        List<Integer> list = Lists.newArrayList(1,2,3,4,5);
        Set<Integer> set = Sets.newHashSet(6,7,8,9);
        Map<String, Integer> map = Maps.newHashMap();
        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);
        
        NetworkUtils.writeTyped(out, address);
        NetworkUtils.writeTyped(out, id);
        NetworkUtils.writeTyped(out, storable);
        NetworkUtils.writeTyped(out, list);
        NetworkUtils.writeTyped(out, set);
        NetworkUtils.writeTyped(out, map);
        
        // Now do the reads
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        
        assertEquals((byte)5, NetworkUtils.readTyped(in));
        assertEquals((short)10, NetworkUtils.readTyped(in));
        assertEquals((int)123456, NetworkUtils.readTyped(in));
        assertEquals((long)89712398712L, NetworkUtils.readTyped(in));
        assertEquals((float)1.0f, NetworkUtils.readTyped(in));
        assertEquals((double)3.0f, NetworkUtils.readTyped(in));
        assertEquals(true, NetworkUtils.readTyped(in));
        assertEquals('4', NetworkUtils.readTyped(in));
        assertEquals("string", NetworkUtils.readTyped(in));
        
        assertEquals(address, NetworkUtils.readTyped(in));
        assertEquals(id, NetworkUtils.readTyped(in));
        assertEquals(storable, NetworkUtils.readTyped(in));
        assertEquals(list, NetworkUtils.readTyped(in));
        assertEquals(set, NetworkUtils.readTyped(in));
        assertEquals(map, NetworkUtils.readTyped(in));
    }
    
    public static class TestByteStorable implements ByteStorable {
        private int value;
        public TestByteStorable() {}
        
        public TestByteStorable(int value) {
            this.value = value;
        }
        
        @Override
        public void save(DataOutput out) throws IOException {
            out.writeInt(value);
        }
        
        @Override
        public void load(DataInput in) throws IOException {
            value = in.readInt();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TestByteStorable)) {
                return false;
            }
            
            return value == ((TestByteStorable)obj).value;
        }
    }
}
