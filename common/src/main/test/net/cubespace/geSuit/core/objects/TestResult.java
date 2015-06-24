package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.cubespace.geSuit.core.objects.Result.Type;

import org.junit.Test;

public class TestResult {
    @Test
    public void testSaveLoadMessage() throws IOException {
        // Test result with message
        Result result = new Result(Type.Success, "This is a test message");
        
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        result.save(out);
        
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        Result inResult = new Result();
        inResult.load(in);
        
        assertEquals(Type.Success, result.getType());
        assertEquals("This is a test message", result.getMessage());
    }
    
    @Test
    public void testSaveLoadNoMessage() throws IOException {
        // Test without message
        Result result = new Result(Type.Success, null);
        
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        result.save(out);
        
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        Result inResult = new Result();
        inResult.load(in);
        
        assertEquals(Type.Success, result.getType());
        assertNull(result.getMessage());
    }
}
