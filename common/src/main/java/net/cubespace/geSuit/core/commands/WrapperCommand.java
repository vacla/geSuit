package net.cubespace.geSuit.core.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;

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
        
        Class<?> senderType;
        if (!isCommandSender(params[0])) {
            if (!CommandContext.class.equals(params[0])) {
                throw new IllegalArgumentException(String.format("Method %s in class %s requires the first parameter be of type CommandSender, a subclass of CommandSender, or a CommandContext to be used as a command", method.getName(), method.getDeclaringClass().getName()));
            } else {
                Type raw = method.getGenericParameterTypes()[0];
                Type argType = ((ParameterizedType)raw).getActualTypeArguments()[0];
                if (argType instanceof Class<?>) {
                    if (!isCommandSender((Class<?>)argType)) {
                        throw new IllegalArgumentException(String.format("Method %s in class %s has invalid CommandContext type %s. The parameter must be CommandSender or a subclass.", method.getName(), method.getDeclaringClass().getName(), ((Class<?>)argType).getName()));
                    } else {
                        senderType = (Class<?>)argType;
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Method %s in class %s has invalid CommandContext type %s. The parameter must be CommandSender or a subclass.", method.getName(), method.getDeclaringClass().getName(), argType));
                }
            }
        } else {
            senderType = params[0];
        }
        
        if (!method.getReturnType().equals(Void.TYPE) && !method.getReturnType().equals(Void.class)) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires the return type to be void", method.getName(), method.getDeclaringClass().getName()));
        }
        
        CommandPriority priorityTag = method.getAnnotation(CommandPriority.class);
        int priority = 0;
        if (priorityTag != null) {
            priority = priorityTag.value();
        }
        
        variants.add(new MethodWrapper(method, tag.async(), priority, tag, senderType));
        if (getUsage0().isEmpty()) {
            setUsage0(tag.usage());
        } else { 
            setUsage0(getUsage0() + "\n" + tag.usage());
        }
    }
    
    public void bake() {
        Collections.sort(variants);
        
        List<Variant> methods = Lists.newArrayListWithCapacity(variants.size());
        for (MethodWrapper variant : variants) {
            methods.add(Variant.fromMethod(methods.size(), variant.method));
        }
        
        ParseTreeBuilder builder = new ParseTreeBuilder(methods);
        ParseNode root = builder.build();
        
        parseTree = new ParseTree(root, methods);
    }
    
    private Object[] createEmptyParameters(Method method) {
        Class<?>[] params = method.getParameterTypes();
        Object[] emptyParams = new Object[params.length];
        
        for (int i = 1; i < emptyParams.length; ++i) {
            Class<?> param = params[i];
            if (param.isPrimitive()) {
                if (param.equals(Byte.TYPE)) {
                    emptyParams[i] = (byte)0;
                } else if (param.equals(Short.TYPE)) {
                    emptyParams[i] = (short)0;
                } else if (param.equals(Integer.TYPE)) {
                    emptyParams[i] = (int)0;
                } else if (param.equals(Long.TYPE)) {
                    emptyParams[i] = (long)0;
                } else if (param.equals(Float.TYPE)) {
                    emptyParams[i] = (float)0;
                } else if (param.equals(Double.TYPE)) {
                    emptyParams[i] = (double)0;
                } else if (param.equals(Character.TYPE)) {
                    emptyParams[i] = (char)0;
                } else if (param.equals(Boolean.TYPE)) {
                    emptyParams[i] = false;
                } else {
                    throw new AssertionError();
                }
            }
        }
        
        return emptyParams;
    }
    
    @Override
    public boolean execute(final Object sender, String label, String[] args) {
        try {
            ParseResult result = parseTree.parse(args);
            MethodWrapper variant = variants.get(result.variant);
            
            final Invokable<Object, Void> method = parseTree.getVariant(result.variant);
            System.out.println("Parse complete: " + method.toString() + " in " + method.getDeclaringClass().getName());
            
            // Ensure the caller is the right type
            if (!variant.senderType.isInstance(sender)) {
                displayWrongSenderError(sender);
                return true;
            }
            
            // Make parameter list
            final Object[] parameters = new Object[result.parameters.size() + 1];
            
            if (variant.useContext) {
                parameters[0] = createContext(sender, variant.tag, label);
            } else {
                parameters[0] = sender;
            }
            
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
        } catch (CommandSyntaxException e) {
            // Syntax doesnt match at all
            
            sendMessage(sender, getUsage0().replace("<command>", label));
        } catch (CommandInterpretException e) {
            // Possible match, but content cant convert
            
            MethodWrapper variant = variants.get(e.getNode().getVariant());
            if (variant.useContext) {
                // If the sender is not the right type, only show the usage
                if (!variant.senderType.isInstance(sender)) {
                    sendMessage(sender, getUsage0().replace("<command>", label));
                    return true;
                // Execute the method to handle error messages
                } else {
                    Object[] parameters = createEmptyParameters(variant.method);
                    CommandContext<?> context = createErrorContext(sender, variant.tag, label, e.getCause(), e.getNode().getArgumentIndex(), e.getInput());
                    parameters[0] = context;
                    execute(sender, parseTree.getVariant(e.getNode().getVariant()), parameters);
                    
                    if (context.getErrorMessage() == null) {
                        sendMessage(sender, getUsage0().replace("<command>", label));
                    } else {
                        sendMessage(sender, "&c" + context.getErrorMessage());
                    }
                }
            // For non context mode, just display the errors message
            } else {
                sendMessage(sender, "&c" + e.getCause().getMessage());
            }
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
        public Command tag;
        public Class<?> senderType;
        public boolean useContext;
        
        public MethodWrapper(Method method, boolean async, int priority, Command tag, Class<?> senderType) {
            this.method = method;
            this.priority = priority;
            this.async = async;
            this.tag = tag;
            this.senderType = senderType;
            
            useContext = method.getParameterTypes()[0].equals(CommandContext.class);
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
