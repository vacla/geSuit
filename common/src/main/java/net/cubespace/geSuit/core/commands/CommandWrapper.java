package net.cubespace.geSuit.core.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.CommandBuilder.CommandDefinition;
import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;

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
            emptyParams[i] = createEmtpyParameter(params[i]);
        }
        
        return emptyParams;
    }
    
    private Object createEmtpyParameter(Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(Byte.TYPE)) {
                return (byte)0;
            } else if (type.equals(Short.TYPE)) {
                return (short)0;
            } else if (type.equals(Integer.TYPE)) {
                return (int)0;
            } else if (type.equals(Long.TYPE)) {
                return (long)0;
            } else if (type.equals(Float.TYPE)) {
                return (float)0;
            } else if (type.equals(Double.TYPE)) {
                return (double)0;
            } else if (type.equals(Character.TYPE)) {
                return (char)0;
            } else if (type.equals(Boolean.TYPE)) {
                return false;
            } else {
                throw new AssertionError();
            }
        } else {
            return null;
        }
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
            if (!variant.senderType.isInstance(sender.getSender())) {
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
    
    public Iterable<String> tabComplete(CommandSenderProxy sender, String label, String[] args) {
        ParseResult result;
        Set<CommandDefinition> options;
        String input;
        int argument;
        try {
            result = parseTree.parse(args);
            // Successful parse should tab complete the last parameter
            options = Sets.newHashSet(variants.get(result.variant));
            argument = result.parameters.size()-1;
            input = result.input.get(argument);
        } catch (ArgumentParseException e) {
            // Tab complete the one that failed
            result = e.getPartialResult();
            argument = e.getNode().getArgumentIndex();
            // When we are short an argument, we have to go back to the previous item
            // otherwise we will be tab completing the missing argument
            if (e instanceof CommandSyntaxException) {
                if (!((CommandSyntaxException)e).hasMoreInput() && argument > 0) {
                    // We were short an argument
                    --argument;
                }
            }
            
            options = Sets.newHashSet();
            for (ParseNode node : e.getChoices()) {
                options.add(variants.get(node.getVariant()));
            }
            
            if (e instanceof CommandInterpretException) {
                input = ((CommandInterpretException)e).getInput();
            } else {
                if (!((CommandSyntaxException)e).hasMoreInput()) {
                    if (!result.input.isEmpty()) {
                        input = result.input.get(result.input.size()-1);
                    } else {
                        input = "";
                    }
                } else {
                    // We have no further parameters to tab complete
                    return null;
                }
            }
        }
        
        return doTabCompleteAt(sender, argument, input, result, options);
    }
    
    private Iterable<String> doTabCompleteAt(CommandSenderProxy sender, int argument, String input, ParseResult result, Set<CommandDefinition> options) {
        Iterable<String> results = null;
        
        
        for (CommandDefinition option : options) {
            if (option.tabCompleter == null) {
                continue;
            }
            
            Parameter p;
            Class<?>[] paramTypes = option.tabCompleter.getParameterTypes();
            Object[] parameters = new Object[paramTypes.length];
            // CommandSender caller
            parameters[0] = sender.getSender();
            // int argument
            parameters[1] = argument;
            // String input
            parameters[2] = input;
            
            // The rest of the params
            for (int i = 3; i < parameters.length; ++i) {
                int actual = i-3;
                if (result.parameters.size() <= actual) {
                    parameters[i] = createEmtpyParameter(paramTypes[i]);
                } else {
                    parameters[i] = result.parameters.get(actual);
                }
            }
            
            // Raise the tab completer
            try {
                Iterable<String> out = (Iterable<String>)option.tabCompleter.invoke(commandHolder, parameters);
                if (out != null) {
                    if (results != null) {
                        results = Iterables.concat(results, out);
                    } else {
                        results = out;
                    }
                }
            } catch (InvocationTargetException e) {
                Throwables.propagateIfPossible(e.getCause());
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                // Should have full access by now
                throw new AssertionError();
            } catch (IllegalArgumentException e) {
                // Should have the correct inputed arguments
                throw new AssertionError(e);
            }
        }
        
        return results;
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
    
    protected abstract void runAsync(Runnable block);
}
