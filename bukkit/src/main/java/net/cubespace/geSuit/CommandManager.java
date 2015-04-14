package net.cubespace.geSuit;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import net.cubespace.geSuit.commands.Command;
import net.cubespace.geSuit.commands.WrapperCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CommandManager {
    private GSPlugin plugin;
    private SimpleCommandMap commandMap;
    private Map<String, WrapperCommand> commands;
    private Set<String> registered; 
    
    public CommandManager(GSPlugin plugin) {
        this.plugin = plugin;
        
        commands = Maps.newHashMap();
        registered = Sets.newHashSet();
        
        try
        {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            commandMap = (SimpleCommandMap)method.invoke(Bukkit.getServer());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void registerAll(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            Command tag = method.getAnnotation(Command.class);
            
            if (tag == null) {
                continue;
            }
            
            registerCommand0(instance, method, tag);
        }
        finishRegistration();
    }
    
    public void registerCommand(Object instance, Method method) {
        Command tag = method.getAnnotation(Command.class);
        
        if (tag == null) {
            throw new IllegalArgumentException("Methods that will be commands require the @Command annotation");
        }
        
        registerCommand0(instance, method, tag);
        finishRegistration();
    }
    
    /*
     * Adds the command to the list or adds variants, but doesnt actually register 
     */
    private void registerCommand0(Object instance, Method method, Command tag) {
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
        for (WrapperCommand command : commands.values()) {
            try {
                command.bake();
                commandMap.register("gesuit", command);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
