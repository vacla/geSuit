package net.cubespace.geSuit.core.util;

import com.google.common.base.Predicate;

public final class CustomPredicates {
    /**
     * Creates a predicate that matches strings that start with a value
     * @param prefix The prefix that needs to match
     * @return A Predicate for strings
     */
    public static Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.startsWith(prefix);
            }
        };
    }
    
    /**
     * Creates a predicate that matches strings that start with a value ignoring case
     * @param prefix The prefix that needs to match, case insensitive
     * @return A Predicate for strings
     */
    public static Predicate<String> startsWithCaseInsensitive(String prefix) {
        final String fPrefix = prefix.toLowerCase();
        
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.toLowerCase().startsWith(fPrefix);
            }
        };
    }
}
