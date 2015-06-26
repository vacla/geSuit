package net.cubespace.geSuit.core.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.CommandBuilder.CommandDefinition;
import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;
import com.google.common.reflect.Invokable;

public class WrapperCommand extends GSCommand {
    private Object commandHolder;
    private List<CommandDefinition> variants;
    private ParseTree parseTree;
    
    public WrapperCommand(Object plugin, Object holder, String name, String[] aliases, String permission, String usage, String description, ParseTree parseTree, List<CommandDefinition> variants) {
        super(name, plugin);
        
        this.commandHolder = holder;
        this.parseTree = parseTree;
        this.variants = variants;
        
        setUsage0(usage);
        
        if (description != null) {
            setDescription0(description);
        }
        
        if (aliases.length > 0) {
            setAliases0(aliases);
        }
        
        if (permission != null) {
            setPermission0(permission);
        }
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
            CommandDefinition variant = variants.get(result.variant);
            
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
            
            CommandDefinition variant = variants.get(e.getNode().getVariant());
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
}
