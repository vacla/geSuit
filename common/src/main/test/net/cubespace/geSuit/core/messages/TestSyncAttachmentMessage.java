package net.cubespace.geSuit.core.messages;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.attachments.Homes;

import org.junit.Test;

import com.google.common.collect.Maps;

public class TestSyncAttachmentMessage {

    @Test
    public void testIO() throws IOException {
        UUID testUUID = UUID.randomUUID();
        Class<? extends Attachment> testClass = Homes.class;
        
        Map<String, String> values = Maps.newHashMap();
        values.put("1", "test1");
        values.put("2", "test2");
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(testUUID, testClass, values);
        
        // Write it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        message.write(out);
        
        // Read it
        message = new SyncAttachmentMessage();
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        message.read(in);
        
        // Test it
        assertEquals(testUUID, message.owner);
        assertEquals(testClass.getName(), message.className);
        assertEquals(values, message.values);
    }

}
