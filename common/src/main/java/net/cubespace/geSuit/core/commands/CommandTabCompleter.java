package net.cubespace.geSuit.core.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.cubespace.geSuit.core.storage.DataConversion;

/**
 * Apply this annotation to any method you wish to make into a command tab completer
 * 
 * <h2>Method Requirements</h2>
 * <ul>
 *  <li>The first parameter must be a CommandSender</li>
 *  <li>The second parameter must be an integer</li>
 *  <li>The third parameter must be a String</li>
 *  <li>The return type must be {@code Iterable<String>}</li>
 *  <li>Each parameter (except the first, second, and third) must be a type that is serializable through the {@link DataConversion} system</li>
 * </ul>
 * 
 * <h2>Linking to commands</h2>
 * <p>For the tab completer to be useful, it must be linked to a command defined with the {@link Command} annotation.
 * To link the tab completer to a command the {@code name} must match {@link Command#name()} in the
 * annotation on the target command and the parameters (excluding {@link Optional} and {@link Varargs} annotations)
 * must be exactly identical.</p>
 * 
 * <h2>Command Priority</h2>
 * <p>The tab completer will use the same priority of the target command.</p>
 * 
 * <h2>Execution</h2>
 * <p>When the tab completer is invoked the arguments will be populated as available.
 * The first parameter (CommandSender) is the caller.
 * The second parameter is the index (not including the 3 fixed params) of the parameter that needs to be filled.
 * The third parameter (String) will be filled with the currently input value. Use this to determine the list of options
 * to show.</p>
 * 
 * <p>All parameters that have been parsed (are before the one being tab completed now) will be filled, all others will be
 * null or in the case of primitives, their default value</p>
 * 
 * <p>The return value should be an {@code Iterable<String>} providing all matching options. This value can be {@code null}
 * and no results will be shown</p>
 * 
 * <h2>Command Variants</h2>
 * <p>When there are multiple possible command variants that can be tab completed at any spot, all relevant tab completers
 * will be called and the results combined</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandTabCompleter {
    /**
     * The name of the command to link to.
     */
    public String name();
}
