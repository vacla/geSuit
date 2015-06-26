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

abstract class CommandWrapper {
    private Object commandHolder;
    private List<CommandDefinition> variants;
    private ParseTree parseTree;
    
    private String name;
    private Object plugin;
    private String usage;
    private String description;
    private String[] aliases;
    private String permission;
    
    public CommandWrapper(Object plugin, Object holder, CommandBuilder builder) {
        this.plugin = plugin;
        this.commandHolder = holder;
        this.name = builder.getName();
        this.aliases = builder.getAliases();
        this.permission = builder.getPermission();
        this.usage = builder.getUsage();
        this.description = builder.getDescription();
        this.parseTree = builder.getParseTree();
        this.variants = builder.getVariants();
    }
    
    public String getName() {
        return name;
    }
    
    public Object getPlugin() {
        return plugin;
    }
    
    public String getUsage() {
        return usage;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String[] getAliases() {
        return aliases;
    }
    
    public String getPermission() {
        return permission;
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
    
    public void execute(CommandSenderProxy sender, String label, String[] args) {
        try {
            ParseResult result = parseTree.parse(args);
            onParseComplete(sender, label, result);
        } catch (CommandSyntaxException e) {
            // Syntax doesnt match at all
            sender.sendMessage(getUsage().replace("<command>", label));
        } catch (CommandInterpretException e) {
            // Possible match, but content cant convert
            onParseError(sender, label, e);
        }
    }
    
    private void onParseComplete(final CommandSenderProxy sender, String label, ParseResult result) {
        CommandDefinition variant = variants.get(result.variant);
        
        final Invokable<Object, Void> method = parseTree.getVariant(result.variant);
        System.out.println("Parse complete: " + method.toString() + " in " + method.getDeclaringClass().getName());
        
        // Ensure the caller is the right type
        if (!variant.senderType.isInstance(sender.getSender())) {
            if (sender.isPlayer()) {
                sender.sendMessage("&cYou are unable to run this command. You must run it from a console.");
            } else {
                sender.sendMessage("&cYou are unable to run this command. You must run it from in game.");
            }
            return;
        }
        
        // Make parameter list
        final Object[] parameters = new Object[result.parameters.size() + 1];
        
        // Inject sender
        if (variant.useContext) {
            parameters[0] = sender.asContext(variant.tag, label);
        } else {
            parameters[0] = sender.getSender();
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
    }
    
    private void onParseError(CommandSenderProxy sender, String label, CommandInterpretException e) {
        CommandDefinition variant = variants.get(e.getNode().getVariant());
        if (variant.useContext) {
            // If the sender is not the right type, only show the usage
            if (!variant.senderType.isInstance(sender)) {
                sender.sendMessage(getUsage().replace("<command>", label));
                return;
            // Execute the method to handle error messages
            } else {
                Object[] parameters = createEmptyParameters(variant.method);
                CommandContext<?> context = sender.asErrorContext(variant.tag, label, e.getCause(), e.getNode().getArgumentIndex(), e.getInput());
                parameters[0] = context;
                execute(sender, parseTree.getVariant(e.getNode().getVariant()), parameters);
                
                if (context.getErrorMessage() == null) {
                    sender.sendMessage(getUsage().replace("<command>", label));
                } else {
                    sender.sendMessage("&c" + context.getErrorMessage());
                }
            }
        // For non context mode, just display the errors message
        } else {
            sender.sendMessage("&c" + e.getCause().getMessage());
        }
    }
    
    private void execute(CommandSenderProxy sender, Invokable<Object, Void> method, Object[] params) {
        try {
            method.invoke(commandHolder, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                sender.sendMessage("&c" + e.getCause().getMessage());
            } else {
                Global.getPlatform().getLogger().log(Level.SEVERE, "An error occured while executing command " + getName(), e.getCause());
                sender.sendMessage("&cAn internal error occured while executing this command");
            }
        } catch (IllegalAccessException e) {
            // Should not happen
            throw new AssertionError(e);
        }
    }
    
    public List<String> tabComplete(CommandSenderProxy sender, String label, String[] args) throws IllegalArgumentException {
        // TODO: This needs to be implemented at some point
        return null;
    }
    
    protected abstract void runAsync(Runnable block);
}
