package net.cubespace.geSuit.core.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command priority allows you to enforce a specific ordering on same named commands. <br>
 * The order is from the lowest number to the highest number.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPriority {
    public int value();
}
