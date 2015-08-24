package net.cubespace.geSuit.core.storage;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRedisSection {
    @Test
    public void testOffsetRoot() {
        RedisSection root = new RedisSection("test.other");
        
        assertEquals("test.other", root.getCurrentPath());
        assertEquals("other", root.getName());
        assertNull(root.getParent());
        
        // Now try to do a subsection
        RedisSection section = root.getSubsection("next");
        assertEquals("test.other.next", section.getCurrentPath());
    }

    @Test
    public void testSubsection() {
        RedisSection root = new RedisSection("");
        RedisSection section = root.getSubsection("test");
        RedisSection section2 = root.getSubsection("test.other");
        
        // Ensure all three objects are separate
        assertNotEquals(root, section);
        assertNotEquals(section, section2);
        
        // Ensure that "test" is a child of root and that "other" is a child of "test"
        assertSame(section.getParent(), root);
        assertSame(section2.getParent(), section);
        assertSame(section.getSubsection("other"), section2);
    }
}
