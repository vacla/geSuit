package net.cubespace.geSuit.core.storage;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.WarnAction.ActionType;

import org.junit.Test;

import com.google.common.base.Converter;

public class TestDataConversion {
    private <T> void testConverter(Converter<String, T> converter, T value) {
        // Convert it to string and back. Must be the same value
        assertEquals(value, converter.convert(converter.reverse().convert(value)));
    }
    
    @Test
    public void testBoolean() {
        testConverter(DataConversion.getConverter(Boolean.class), true);
    }
    
    @Test
    public void testUUID() {
        testConverter(DataConversion.getConverter(UUID.class), UUID.randomUUID());
    }
    
    @Test
    public void testInetAddress() throws UnknownHostException {
        testConverter(DataConversion.getConverter(InetAddress.class), InetAddress.getByName("127.0.0.1"));
    }
    
    @Test
    public void testDateDiff() {
        testConverter(DataConversion.getConverter(DateDiff.class), new DateDiff(TimeUnit.MINUTES.toMillis(4) + TimeUnit.SECONDS.toMillis(43)));
    }
    
    @Test
    public void testEnum() {
        testConverter(DataConversion.getConverter(ActionType.class), ActionType.Kick);
    }
    
    @Test
    public void testPrimitive() {
        testConverter(DataConversion.getConverter(Integer.TYPE), 5);
    }
}
