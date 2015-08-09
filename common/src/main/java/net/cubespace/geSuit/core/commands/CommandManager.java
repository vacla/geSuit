package net.cubespace.geSuit.core.commands;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This command system allows quick and easy creation of commands without the need for each command to have their own class or 
 * to have a bunch of switch / if statements. This also simplifies execution with automatic parsing of arguments.
 * 
 * See {@link Command} for a detailed description of how to make the commands
 */
public abstract class CommandManager {
    private Map<String, CommandBuilder> builders;
    private Set<String> registered; 
    
    public CommandManager() {
        builders = Maps.newHashMap();
        registered = Sets.newHashSet();
    }
    
    /**
     * Registers all commands in {@code instance} annotated with {@link Command}. 
     * @param instance The object that contains the command methods
     * @param plugin The owning plugin. This is not typed as this is a platform agnostic class.
     */
    public void registerAll(Object instance, Object plugin) {
        // Register commands first
        for (Method method : instance.getClass().getDeclaredMethods()) {
            Command tag = method.getAnnotation(Command.class);
            
            if (tag == null) {
                continue;
            }
            
            registerCommand0(instance, method, tag);
        }
        
        // Register tab completers next
        for (Method method : instance.getClass().getDeclaredMethods()) {
            CommandTabCompleter tag = method.getAnnotation(CommandTabCompleter.class);
            
            if (tag == null) {
                continue;
            }
            
            registerTabCompleter0(instance, method, tag);
        }
        finishRegistration(plugin, instance);
    }
    
    /**
     * Registers the {@code method} in {@code instance} as a command. The method still requires the {@link Command} annotation 
     * @param instance The object that contains the method
     * @param method The method object for the command
     * @param plugin The owning plugin. This is not typed as this is a platform agnostic class.
     */
    public void registerCommand(Object instance, Method method, Object plugin) {
        Command tag = method.getAnnotation(Command.class);
        
        if (tag == null) {
            throw new IllegalArgumentException("Methods that will be commands require the @Command annotation");
        }
        
        registerCommand0(instance, method, tag);
        finishRegistration(plugin, instance);
    }
    
    /**
     * Registers the {@code command} as a command and the {@code tabCompleter} as a tab completer. Both method still require the {@link Command} {@link CommandTabCompleter} annotations respectively 
     * @param instance The object that contains the methods
     * @param command The method object for the command
     * @param tabCompleter The method object for the tab completer
     * @param plugin The owning plugin. This is not typed as this is a platform agnostic class.
     */
    public void registerCommand(Object instance, Method command, Method tabCompleter, Object plugin) {
        Command commandTag = command.getAnnotation(Command.class);
        
        if (commandTag == null) {
            throw new IllegalArgumentException("Methods that will be commands require the @Command annotation");
        }
        
        CommandTabCompleter tabCompleteTag = command.getAnnotation(CommandTabCompleter.class);
        
        if (tabCompleteTag == null) {
            throw new IllegalArgumentException("Methods that will be tab completeres require the @CommandTabCompleter annotation");
        }
        
        registerCommand0(instance, command, commandTag);
        registerTabCompleter0(instance, tabCompleter, tabCompleteTag);
        finishRegistration(plugin, instance);
    }
    
    /*
     * Adds the command to the list or adds variants, but doesnt actually register 
     */
    private void registerCommand0(Object instance, Method method, Command tag) {
        if (registered.contains(tag.name())) {
            throw new IllegalArgumentException("The command " + tag.name() + " has already been registered. You can only register vairants of a command using registerAll");
        }
        
        CommandBuilder builder;
        if (builders.containsKey(tag.name())) {
            builder = builders.get(tag.name());
        } else {
            builder = createBuilder();
            builders.put(tag.name(), builder);
        }
        
        builder.addVariant(method);
    }
    
    private void registerTabCompleter0(Object instance, Method method, CommandTabCompleter tag) {
        if (registered.contains(tag.name())) {
            throw new IllegalArgumentException("The command " + tag.name() + " has already been registered. You can only register tab completeres for a command before it is registered");
        }
        
        CommandBuilder builder = builders.get(tag.name());
        if (builder == null) {
            throw new IllegalArgumentException("Unknown command " + tag.name() + ". Unable to register tab completer");
        }
        
        builder.addTabCompleter(method);
    }
    
    /*
     * Finishes the registration making the commands fully available for use
     */
    private void finishRegistration(Object plugin, Object holder) {
        List<CommandWrapper> commands = Lists.newArrayListWithCapacity(builders.size());
        for (CommandBuilder builder : builders.values()) {
            builder.build();
            commands.add(createCommand(plugin, holder, builder));
        }
        
        installCommands(commands);
    }
    
    protected abstract CommandBuilder createBuilder();
    protected abstract CommandWrapper createCommand(Object plugin, Object holder, CommandBuilder builder);
    protected abstract void installCommands(Collection<CommandWrapper> commands);
}
