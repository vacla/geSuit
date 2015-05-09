package net.cubespace.geSuit.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.commands.parser.ArgumentParseException;
import net.cubespace.geSuit.commands.parser.ParseTree;
import net.cubespace.geSuit.commands.parser.ParseTree.ParseResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;

public class WrapperCommand extends Command implements PluginIdentifiableCommand {
    private Plugin plugin;
    
    private Object commandHolder;
    private List<MethodWrapper> variants;
    private ParseTree parseTree;
    
    public WrapperCommand(Plugin plugin, Object holder, Method method, net.cubespace.geSuit.commands.Command tag) {
        super(tag.name());
        
        this.plugin = plugin;
        commandHolder = holder;
        
        variants = Lists.newArrayList();
        setUsage("");
        
        if (!tag.description().isEmpty()) {
            setDescription(tag.description());
        }
        if (tag.aliases().length != 0) {
            setAliases(Arrays.asList(tag.aliases()));
        }
        if (!tag.permission().isEmpty()) {
            setPermission(tag.permission());
        }
        
        addVariant(method, tag);
    }
    
    public void addVariant(Method method, net.cubespace.geSuit.commands.Command tag) {
        method.setAccessible(true);
        
        Class<?>[] params = method.getParameterTypes();
        
        if (params.length == 0) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires at least one parameter of type CommandSender or subclass to be used as a command", method.getName(), method.getDeclaringClass().getName()));
        }
        
        if (!CommandSender.class.isAssignableFrom(params[0])) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires the first parameter be of type CommandSender or subclass to be used as a command", method.getName(), method.getDeclaringClass().getName()));
        }
        
        if (!method.getReturnType().equals(Void.TYPE) && !method.getReturnType().equals(Void.class)) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires the return type to be void", method.getName(), method.getDeclaringClass().getName()));
        }
        
        CommandPriority priorityTag = method.getAnnotation(CommandPriority.class);
        int priority = 0;
        if (priorityTag != null) {
            priority = priorityTag.value();
        }
        
        variants.add(new MethodWrapper(method, tag.async(), priority));
        if (getUsage().isEmpty()) {
            setUsage(tag.usage());
        } else { 
            setUsage(getUsage() + "\n" + tag.usage());
        }
    }
    
    public void bake() {
        Collections.sort(variants);
        
        List<Method> methods = Lists.newArrayListWithCapacity(variants.size());
        for (MethodWrapper variant : variants) {
            methods.add(variant.method);
        }
        
        parseTree = new ParseTree(methods);
        parseTree.build();
    }

    @Override
    public boolean execute(final CommandSender sender, String label, String[] args) {
        try {
            System.out.println("Executing command " + label);
            
            ParseResult result = parseTree.parse(args);
            final Invokable<Object, Void> method = parseTree.getVariant(result.variant);
            System.out.println("Parse complete: " + method.toString() + " in " + method.getDeclaringClass().getName());
            
            
            // Ensure the caller is the right type
            Parameter senderParam = method.getParameters().get(0);
            if (!senderParam.getType().getRawType().isInstance(sender)) {
                if (sender instanceof Player) {
                    sender.sendMessage(ChatColor.RED + "You are unable to run this command. You must run it from a console.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You are unable to run this command. You must run it from in game.");
                }
                return true;
            }
            
            // Make parameter list
            final Object[] parameters = new Object[result.parameters.size() + 1];
            parameters[0] = sender;
            int index = 1;
            for (Object object : result.parameters) {
                parameters[index++] = object;
            }
            
            System.out.println("params: " + Arrays.toString(parameters));
            
            // Invoke the method
            if (variants.get(result.variant).async) {
                Bukkit.getScheduler().runTaskAsynchronously(GSPlugin.getPlugin(GSPlugin.class), new Runnable() {
                    @Override
                    public void run() {
                        execute(sender, method, parameters);
                    }
                });
            } else {
                execute(sender, method, parameters);
            }
        } catch (ArgumentParseException e) {
            sender.sendMessage(getUsage().replace("<command>", label));
            System.out.println("APE: argument " + e.getArgument() + " in variant " + e.getNode().getVariant() + " value " + e.getValue());
        }
        
        return true;
    }
    
    private void execute(CommandSender sender, Invokable<Object, Void> method, Object[] params) {
        try {
            method.invoke(commandHolder, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                sender.sendMessage(ChatColor.RED + e.getCause().getMessage());
            } else {
                plugin.getLogger().log(Level.SEVERE, "An error occured while executing command " + getName(), e.getCause());
                sender.sendMessage(ChatColor.RED + "An internal error occured while executing this command");
            }
        } catch (IllegalAccessException e) {
            // Should not happen
            throw new AssertionError(e);
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        return null;
    }
    
    @Override
    public Plugin getPlugin() {
        return plugin;
    }
    
    private static class MethodWrapper implements Comparable<MethodWrapper> {
        public Method method;
        public int priority;
        public boolean async;
        
        public MethodWrapper(Method method, boolean async, int priority) {
            this.method = method;
            this.priority = priority;
            this.async = async;
        }

        @Override
        public int compareTo(MethodWrapper other) {
            return Integer.compare(other.priority, priority);
        }
        
        @Override
        public String toString() {
            return String.format("%d %s", priority, method);
        }
    }
}
