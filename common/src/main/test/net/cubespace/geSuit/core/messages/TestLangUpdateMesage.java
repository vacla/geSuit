package net.cubespace.geSuit.core.messages;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

public class TestLangUpdateMesage {
    @Test
    public void testCombine() {
        Properties defaults = new Properties();
        Properties messages = new Properties(defaults);
        
        defaults.setProperty("test.1", "The default value");
        defaults.setProperty("test.2", "Not present");
        
        messages.setProperty("test.1", "Overridden");
        messages.setProperty("test.3", "Only in messages");
        
        LangUpdateMessage packet = new LangUpdateMessage(messages, defaults);
        
        assertEquals("Overridden", packet.messages.getProperty("test.1"));
        assertEquals("Not present", packet.messages.getProperty("test.2"));
        assertEquals("Only in messages", packet.messages.getProperty("test.3"));
        assertNull(packet.messages.getProperty("test.4"));
    }

    @Test
    public void testIO() throws IOException {
        Properties defaults = new Properties();
        Properties messages = new Properties(defaults);
        
        defaults.setProperty("test.1", "The default value");
        defaults.setProperty("test.2", "Not present");
        
        messages.setProperty("test.1", "Overridden");
        messages.setProperty("test.3", "Only in messages");
        
        LangUpdateMessage packet = new LangUpdateMessage(messages, defaults);
        
        // Write it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        packet.write(out);
        
        // Read it
        packet = new LangUpdateMessage();
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        packet.read(in);
        
        // Test
        assertEquals("Overridden", packet.messages.getProperty("test.1"));
        assertEquals("Not present", packet.messages.getProperty("test.2"));
        assertEquals("Only in messages", packet.messages.getProperty("test.3"));
        assertNull(packet.messages.getProperty("test.4"));
    }
}
