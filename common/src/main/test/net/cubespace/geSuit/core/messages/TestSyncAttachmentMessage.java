package net.cubespace.geSuit.core.messages;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class TestSyncAttachmentMessage {

    @Test
    public void testIO() throws IOException {
        UUID testUUID = UUID.randomUUID();
        Set<String> ids = Sets.newHashSet();
        
        ids.add("test.id.1");
        ids.add("test.id.2");
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(testUUID, ids);
        
        // Write it
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        message.write(out);
        
        // Now read it
        SyncAttachmentMessage actual = new SyncAttachmentMessage();
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        actual.read(in);
        
        // Compare it
        assertEquals(testUUID, actual.owner);
        assertEquals(ids, actual.updatedAttachments);
    }

}
