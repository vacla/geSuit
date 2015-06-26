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
        for (Method method : instance.getClass().getDeclaredMethods()) {
            Command tag = method.getAnnotation(Command.class);
            
            if (tag == null) {
                continue;
            }
            
            registerCommand0(instance, method, tag, plugin);
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
        
        registerCommand0(instance, method, tag, plugin);
        finishRegistration(plugin, instance);
    }
    
    /*
     * Adds the command to the list or adds variants, but doesnt actually register 
     */
    private void registerCommand0(Object instance, Method method, Command tag, Object plugin) {
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
