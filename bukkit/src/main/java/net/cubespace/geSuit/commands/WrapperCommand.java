package net.cubespace.geSuit.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    private List<Method> variants;
    private List<Boolean> executeAsync;
    private ParseTree parseTree;
    
    public WrapperCommand(Plugin plugin, Object holder, Method method, net.cubespace.geSuit.commands.Command tag) {
        super(tag.name());
        
        this.plugin = plugin;
        commandHolder = holder;
        
        variants = Lists.newArrayList();
        executeAsync = Lists.newArrayList();
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
        
        variants.add(method);
        executeAsync.add(tag.async());
        if (getUsage().isEmpty()) {
            setUsage(tag.usage());
        } else { 
            setUsage(getUsage() + "\n" + tag.usage());
        }
        
        // We need a parse tree
        // Each node should be a parseable type
        // The children of those nodes are all the possibilities from that point
        // The leafs of the tree should indicate which method will be called with the data
        // Varargs must be the lowest non leaf level and should be placed at the end of the children for their parent so that other options are considered first
        
        
    }
    
    public void bake() {
        parseTree = new ParseTree(variants);
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
            if (executeAsync.get(result.variant)) {
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
    
}
