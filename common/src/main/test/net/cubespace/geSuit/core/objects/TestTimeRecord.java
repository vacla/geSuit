package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

public class TestTimeRecord {
    @Test
    public void testConstruct1() {
        UUID testUUID = UUID.randomUUID();
        TimeRecord record = new TimeRecord(testUUID);
        assertEquals(testUUID, record.getUniqueId());
    }
    
    @Test
    public void testConstruct2() {
        UUID testUUID = UUID.randomUUID();
        long time = 12345;
        TimeRecord record = new TimeRecord(testUUID, time);
        assertEquals(testUUID, record.getUniqueId());
        assertEquals(time, record.getTimeTotal());
    }
    
    @Test
    public void testSaveLoad() throws IOException {
        // Generate the record
        TimeRecord record = new TimeRecord(UUID.randomUUID(), 6);
        record.setTimeSession(1);
        record.setTimeToday(2);
        record.setTimeWeek(3);
        record.setTimeMonth(4);
        record.setTimeYear(5);
        
        // Save
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        record.save(out);
        
        // Load
        TimeRecord inRecord = new TimeRecord();
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        inRecord.load(in);
        
        // Test
        assertEquals(record.getUniqueId(), inRecord.getUniqueId());
        assertEquals(record.getTimeSession(), inRecord.getTimeSession());
        assertEquals(record.getTimeToday(), inRecord.getTimeToday());
        assertEquals(record.getTimeWeek(), inRecord.getTimeWeek());
        assertEquals(record.getTimeMonth(), inRecord.getTimeMonth());
        assertEquals(record.getTimeYear(), inRecord.getTimeYear());
        assertEquals(record.getTimeTotal(), inRecord.getTimeTotal());
    }

}
