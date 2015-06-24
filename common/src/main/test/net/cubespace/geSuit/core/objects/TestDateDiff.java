package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;
import static java.util.concurrent.TimeUnit.*;

import org.junit.Test;

public class TestDateDiff {

    @Test
    public void testFromNow() {
        DateDiff diff = new DateDiff(1000);
        long fromNow = System.currentTimeMillis() + 1000;
        // Cant do exact as this may randomly fail based on time
        // but it will be at least fromNow
        assertTrue(diff.fromNow() >= fromNow);
    }

    @Test
    public void testFrom() {
        DateDiff diff = new DateDiff(1000);
        assertEquals(2000, diff.from(1000));
    }

    @Test
    public void testToMillis() {
        DateDiff diff = new DateDiff(1234);
        assertEquals(1234, diff.toMillis());
    }

    @Test
    public void testValidValueOf() {
        DateDiff diff = DateDiff.valueOf("5d");
        assertEquals(DAYS.toMillis(5), diff.toMillis());
        
        diff = DateDiff.valueOf("2h4m1s");
        assertEquals(HOURS.toMillis(2) + MINUTES.toMillis(4) + SECONDS.toMillis(1), diff.toMillis());
        
        diff = DateDiff.valueOf("2h 4m");
        assertEquals(HOURS.toMillis(2) + MINUTES.toMillis(4), diff.toMillis());
        
        diff = DateDiff.valueOf("0s");
        assertEquals(0, diff.toMillis());
    }
    
    @Test
    public void testInvalidValueOf() {
        try {
            DateDiff.valueOf("4l");
            fail();
        } catch (IllegalArgumentException e) {
        }
        
        try {
            DateDiff.valueOf("-4s");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testToString() {
        DateDiff diff = new DateDiff(SECONDS.toMillis(1));
        assertEquals("1s", diff.toString());
        
        diff = new DateDiff(MINUTES.toMillis(5) + SECONDS.toMillis(2));
        assertEquals("5m2s", diff.toString());
        
        diff = new DateDiff(HOURS.toMillis(8) + MINUTES.toMillis(2) + SECONDS.toMillis(40));
        assertEquals("8h2m40s", diff.toString());
        
        diff = new DateDiff(DAYS.toMillis(2));
        assertEquals("2d", diff.toString());
        
        diff = new DateDiff(DAYS.toMillis(7));
        assertEquals("1w", diff.toString());
        
        diff = new DateDiff(DAYS.toMillis(30));
        assertEquals("1mo", diff.toString());
        
        diff = new DateDiff(DAYS.toMillis(365));
        assertEquals("1y", diff.toString());
    }

    @Test
    public void testToStringLimited() {
        DateDiff diff = new DateDiff(DAYS.toMillis(25) + HOURS.toMillis(8) + MINUTES.toMillis(2) + SECONDS.toMillis(40));
        assertEquals("3w4d8h2m40s", diff.toString(5));
        assertEquals("3w4d8h2m", diff.toString(4));
        assertEquals("3w4d8h", diff.toString(3));
        assertEquals("3w4d", diff.toString(2));
        assertEquals("3w", diff.toString(1));
    }

    @Test
    public void testToLongString() {
        DateDiff diff = new DateDiff(SECONDS.toMillis(1));
        assertEquals("1 Second", diff.toLongString());
        
        diff = new DateDiff(MINUTES.toMillis(5) + SECONDS.toMillis(2));
        assertEquals("5 Minutes 2 Seconds", diff.toLongString());
        
        diff = new DateDiff(HOURS.toMillis(8) + MINUTES.toMillis(2) + SECONDS.toMillis(40));
        assertEquals("8 Hours 2 Minutes 40 Seconds", diff.toLongString());
        
        diff = new DateDiff(DAYS.toMillis(2));
        assertEquals("2 Days", diff.toLongString());
        
        diff = new DateDiff(DAYS.toMillis(7));
        assertEquals("1 Week", diff.toLongString());
        
        diff = new DateDiff(DAYS.toMillis(30));
        assertEquals("1 Month", diff.toLongString());
        
        diff = new DateDiff(DAYS.toMillis(365));
        assertEquals("1 Year", diff.toLongString());
    }

    @Test
    public void testToLongStringInt() {
        DateDiff diff = new DateDiff(DAYS.toMillis(25) + HOURS.toMillis(8) + MINUTES.toMillis(2) + SECONDS.toMillis(40));
        assertEquals("3 Weeks 4 Days 8 Hours 2 Minutes 40 Seconds", diff.toLongString(5));
        assertEquals("3 Weeks 4 Days 8 Hours 2 Minutes", diff.toLongString(4));
        assertEquals("3 Weeks 4 Days 8 Hours", diff.toLongString(3));
        assertEquals("3 Weeks 4 Days", diff.toLongString(2));
        assertEquals("3 Weeks", diff.toLongString(1));
    }

}
