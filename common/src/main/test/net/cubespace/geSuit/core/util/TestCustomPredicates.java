package net.cubespace.geSuit.core.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TestCustomPredicates {
    @Test
    public void testStartsWith() {
        List<String> rawList = Lists.newArrayList("abcd", "Abc", "dab", "defg");
        Iterable<String> filtered = Iterables.filter(rawList, CustomPredicates.startsWith("ab"));
        
        assertEquals(1, Iterables.size(filtered));
        assertTrue(Iterables.contains(filtered, "abcd"));
    }

    @Test
    public void testStartsWithCaseInsensitive() {
        List<String> rawList = Lists.newArrayList("abcd", "Abc", "dab", "defg");
        Iterable<String> filtered = Iterables.filter(rawList, CustomPredicates.startsWithCaseInsensitive("ab"));
        
        assertEquals(2, Iterables.size(filtered));
        assertTrue(Iterables.contains(filtered, "abcd"));
        assertTrue(Iterables.contains(filtered, "Abc"));
    }
}
