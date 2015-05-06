package net.cubespace.geSuit.core.serialization;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import net.cubespace.geSuit.core.objects.BanInfo;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public class TestSerialization {
    @Test
    public void testDynamicRegister() {
        TypeToken<List<String>> type = new TypeToken<List<String>>() {};
        AdvancedSerializer<List<String>> serializer = Serialization.getSerializer(type);
        
        assertNotNull(serializer);
        assertEquals(type, serializer.getType());
    }
    
    @Test
    public void testStringList() throws IOException {
        TypeToken<List<String>> type = new TypeToken<List<String>>() {};
        
        List<String> source = Lists.newArrayList();
        source.add("test");
        source.add("of");
        source.add("the");
        source.add("string");
        source.add("list");
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(source, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        List<String> result = Serialization.deserialize(type, in);
        
        assertEquals(source, result);
        assertNotSame(result, source);
    }
    
    @Test
    public void testIntList() throws IOException {
        TypeToken<List<Integer>> type = new TypeToken<List<Integer>>() {};
        
        List<Integer> source = Lists.newArrayList();
        source.add(3);
        source.add(1);
        source.add(4);
        source.add(1);
        source.add(5);
        source.add(9);
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(source, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        List<Integer> result = Serialization.deserialize(type, in);
        
        assertEquals(source, result);
        assertNotSame(result, source);
    }
    
    @Test
    public void testDeepList() throws IOException {
        TypeToken<List<List<String>>> type = new TypeToken<List<List<String>>>() {};
        
        List<List<String>> source = Lists.newArrayList();
        source.add(Lists.newArrayList("1-1", "1-2", "1-3"));
        source.add(Lists.newArrayList("2-1", "2-2", "2-3"));
        source.add(Lists.newArrayList("3-1", "3-2", "3-3"));
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(source, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        List<List<String>> result = Serialization.deserialize(type, in);
        
        assertEquals(source, result);
        assertNotSame(result, source);
    }
    
    @Test
    public void testStringSet() throws IOException {
        TypeToken<Set<String>> type = new TypeToken<Set<String>>() {};
        
        Set<String> source = Sets.newHashSet();
        source.add("test");
        source.add("of");
        source.add("the");
        source.add("string");
        source.add("set");
        source.add("test");
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(source, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        Set<String> result = Serialization.deserialize(type, in);
        
        assertEquals(source, result);
        assertNotSame(result, source);
    }
    
    @Test
    public void testDeepSet() throws IOException {
        TypeToken<Set<List<String>>> type = new TypeToken<Set<List<String>>>() {};
        
        Set<List<String>> source = Sets.newHashSet();
        source.add(Lists.newArrayList("1-1", "1-2", "1-3"));
        source.add(Lists.newArrayList("2-1", "2-2", "2-3"));
        source.add(Lists.newArrayList("3-1", "3-2", "3-3"));
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(source, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        Set<List<String>> result = Serialization.deserialize(type, in);
        
        assertEquals(source, result);
        assertNotSame(result, source);
    }
    
    @Test
    public void testByteStorable() throws IOException {
        TypeToken<BanInfo<InetAddress>> type = new TypeToken<BanInfo<InetAddress>>() {};
        
        BanInfo<InetAddress> test = new BanInfo<InetAddress>(InetAddress.getByName("127.0.0.1"), 1, "Test", "Console", null, 1234444L, 0, false);
        
        // Serialize it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        Serialization.serialize(test, type, out);
        
        // Deserialize it
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        
        BanInfo<InetAddress> result = Serialization.deserialize(type, in);
        
        assertEquals(test, result);
        assertNotSame(result, test);
    }
}
