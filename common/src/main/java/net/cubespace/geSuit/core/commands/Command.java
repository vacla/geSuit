package net.cubespace.geSuit.core.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.cubespace.geSuit.core.serialization.Serialization;
import net.cubespace.geSuit.core.storage.DataConversion;

/**
 * Apply this annotation to any method you wish to make into a command
 * 
 * <h2>Method Requirements</h2>
 * <ul>
 *   <li>The first parameter must be a CommandSender or a sub class</li>
 *   <li>The return type must be void</li>
 *   <li>Each parameter (except the first) must be a type that is serializable through the {@link DataConversion} system</li>
 * </ul>
 * 
 * Methods may throw {@link IllegalArgumentException} to provide an error message to the caller.
 * 
 * <h2>Building commands</h2>
 * <p>You can provide several different versions of the same method name which are all available through the same command. 
 * This can simply be done by using the same name for each version. Each version should provide a usage description specific to that version </p>
 * 
 * <p><b>Note:</b> While technically only the first one needs all the information, the order of methods as seen by the JVM are not always 
 * the same as in source code so it is best to provide all information (permission, aliases, description, etc.) on each version of the command.</p>
 * 
 * <p>In cases where one version of the command could be parsed instead of another version, for example a string would cover an int, you can use
 * the annotation {@link CommandPriority} to specify an order on the versions so that the more specific versions are parsed first.</p>
 * 
 * <p>Some times you have an optional parameter and would prefer not to create another method to cover that case. For this you can use the
 * {@link Optional} annotation. Place it on the parameter you wish to be optional. A value of {@code null} is used when the value was omitted 
 * in the command. <b>NOTE:</b> You cannot use this on a primitive type as they cannot be assigned a null value.</p>
 * 
 * <p>When you require a bunch of arguments to be provided as one, for example for a message, you can use the {@link Varargs} annotation
 * which will combine all remaining provided arguments into one item. This works with non string types too as it will attempt to parse the combined
 * text as the target type.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The command name. This is the primary name to the command system
     */
    public String name();
    /**
     * Selects whether to execute this command on the server thread, or another thread.
     */
    public boolean async() default false;
    /**
     * Any other names for this command.
     */
    public String[] aliases() default {};
    /**
     * The permission required to be able to execute this command
     */
    public String permission() default "";
    /**
     * The usage string for this command. Use the token {@code <command>} to be replaced with the name used to execute it 
     */
    public String usage() default "/<command>";
    /**
     * A description of what this command does and how to use it. <b>NOTE: This is not used on BungeeCord</b>
     */
    public String description() default "";
}
