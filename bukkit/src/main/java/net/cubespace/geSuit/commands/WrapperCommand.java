package net.cubespace.geSuit.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.cubespace.geSuit.commands.parser.ArgumentParseException;
import net.cubespace.geSuit.commands.parser.ParseTree;
import net.cubespace.geSuit.commands.parser.ParseTree.ParseResult;

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
        
        variants.add(method);
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
    public boolean execute(CommandSender sender, String label, String[] args) {
        try {
            System.out.println("Executing command " + label);
            
            ParseResult result = parseTree.parse(args);
            Invokable<Object, Void> method = parseTree.getVariant(result.variant);
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
            
            // Now we can invoke the method
            try {
                Object[] parameters = new Object[result.parameters.size() + 1];
                parameters[0] = sender;
                int index = 1;
                for (Object object : result.parameters) {
                    parameters[index++] = object;
                }
                
                System.out.println("params: " + Arrays.toString(parameters));
                
                method.invoke(commandHolder, parameters);
                return true;
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof IllegalArgumentException) {
                    sender.sendMessage(ChatColor.RED + e.getCause().getMessage());
                } else {
                    plugin.getLogger().log(Level.SEVERE, "An error occured while executing command " + getName(), e.getCause());
                    sender.sendMessage(ChatColor.RED + "An internal error occured while executing this command");
                }
                return true;
            } catch (IllegalAccessException e) {
                // Should not happen
                throw new AssertionError(e);
            }
        } catch (ArgumentParseException e) {
            sender.sendMessage(getUsage());
            System.out.println("APE: argument " + e.getArgument() + " in variant " + e.getNode().getVariant() + " value " + e.getValue());
            return true;
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
