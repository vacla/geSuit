package net.cubespace.geSuit.core.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;

import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;

public class WrapperCommand extends GSCommand {
    private Object commandHolder;
    private List<MethodWrapper> variants;
    private ParseTree parseTree;
    
    public WrapperCommand(Object plugin, Object holder, Method method, net.cubespace.geSuit.core.commands.Command tag) {
        super(tag.name(), plugin);
        
        commandHolder = holder;
        
        variants = Lists.newArrayList();
        setUsage0("");
        
        if (!tag.description().isEmpty()) {
            setDescription0(tag.description());
        }
        if (tag.aliases().length != 0) {
            setAliases0(tag.aliases());
        }
        if (!tag.permission().isEmpty()) {
            setPermission0(tag.permission());
        }
        
        addVariant(method, tag);
    }
    
    public void addVariant(Method method, net.cubespace.geSuit.core.commands.Command tag) {
        method.setAccessible(true);
        
        Class<?>[] params = method.getParameterTypes();
        
        if (params.length == 0) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires at least one parameter of type CommandSender or subclass to be used as a command", method.getName(), method.getDeclaringClass().getName()));
        }
        
        if (!isCommandSender(params[0])) {
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
        if (getUsage0().isEmpty()) {
            setUsage0(tag.usage());
        } else { 
            setUsage0(getUsage0() + "\n" + tag.usage());
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
    public boolean execute(final Object sender, String label, String[] args) {
        try {
            System.out.println("Executing command " + label);
            
            ParseResult result = parseTree.parse(args);
            final Invokable<Object, Void> method = parseTree.getVariant(result.variant);
            System.out.println("Parse complete: " + method.toString() + " in " + method.getDeclaringClass().getName());
            
            
            // Ensure the caller is the right type
            Parameter senderParam = method.getParameters().get(0);
            if (!senderParam.getType().getRawType().isInstance(sender)) {
                displayWrongSenderError(sender);
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
                runAsync(new Runnable() {
                    @Override
                    public void run() {
                        execute(sender, method, parameters);
                    }
                });
            } else {
                execute(sender, method, parameters);
            }
        } catch (ArgumentParseException e) {
            sendMessage(sender, getUsage0().replace("<command>", label));
            System.out.println("APE: argument " + e.getArgument() + " in variant " + e.getNode().getVariant() + " value " + e.getValue() + " reason " + e.getReason());
        }
        
        return true;
    }
    
    private void execute(Object sender, Invokable<Object, Void> method, Object[] params) {
        try {
            method.invoke(commandHolder, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                sendMessage(sender, "&c" + e.getCause().getMessage());
            } else {
                Global.getPlatform().getLogger().log(Level.SEVERE, "An error occured while executing command " + getName(), e.getCause());
                sendMessage(sender, "&cAn internal error occured while executing this command");
            }
        } catch (IllegalAccessException e) {
            // Should not happen
            throw new AssertionError(e);
        }
    }
    
    @Override
    public List<String> tabComplete(Object sender, String label, String[] args) throws IllegalArgumentException {
        return null;
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
