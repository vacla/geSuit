package net.cubespace.geSuit.core.commands;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class CommandManager {
    private Map<String, WrapperCommand> commands;
    private Set<String> registered; 
    
    public CommandManager() {
        commands = Maps.newHashMap();
        registered = Sets.newHashSet();
    }
    
    public void registerAll(Object instance, Object plugin) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            Command tag = method.getAnnotation(Command.class);
            
            if (tag == null) {
                continue;
            }
            
            registerCommand0(instance, method, tag, plugin);
        }
        finishRegistration();
    }
    
    public void registerCommand(Object instance, Method method, Object plugin) {
        Command tag = method.getAnnotation(Command.class);
        
        if (tag == null) {
            throw new IllegalArgumentException("Methods that will be commands require the @Command annotation");
        }
        
        registerCommand0(instance, method, tag, plugin);
        finishRegistration();
    }
    
    /*
     * Adds the command to the list or adds variants, but doesnt actually register 
     */
    private void registerCommand0(Object instance, Method method, Command tag, Object plugin) {
        if (registered.contains(tag.name())) {
            throw new IllegalArgumentException("The command " + tag.name() + " has already been registered. You can only register vairants of a command using registerAll");
        }
        
        if (commands.containsKey(tag.name())) {
            WrapperCommand command = commands.get(tag.name());
            command.addVariant(method, tag);
        } else {
            WrapperCommand command = new WrapperCommand(plugin, instance, method, tag);
            commands.put(tag.name(), command);
        }
    }
    
    /*
     * Finishes the registration making the commands fully available for use
     */
    private void finishRegistration() {
        installCommands(commands.values());
    }
    
    protected abstract void installCommands(Collection<WrapperCommand> commands);
}
