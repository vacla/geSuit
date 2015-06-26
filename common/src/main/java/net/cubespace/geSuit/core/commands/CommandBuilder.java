package net.cubespace.geSuit.core.commands;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

abstract class CommandBuilder {
    private List<CommandDefinition> definitions;
    
    private ParseTree parseTree;
    
    // Command details
    private String name;
    private String[] aliases;
    private String description;
    private String fullUsage;
    private String permission; // Wont be set if multiple permissions are in play
    
    public CommandBuilder() {
        definitions = Lists.newArrayList();
    }
    
    public void addVariant(Method method) throws IllegalArgumentException {
        if (!method.isAnnotationPresent(Command.class)) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires the @Command annotation to be used as a command executor", method.getName(), method.getDeclaringClass().getName()));
        }
        
        Command tag = method.getAnnotation(Command.class);
        
        if (name == null) {
            name = tag.name();
        } else if (!tag.name().equals(name)) {
            throw new IllegalArgumentException(String.format("Method %s in class %s is being registered under command %s but is declared as %s", method.getName(), method.getDeclaringClass().getName(), name, tag.name()));
        }
        
        method.setAccessible(true);
        
        Class<?>[] params = method.getParameterTypes();
        
        // Needs at least the sender/context
        if (params.length == 0) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires at least one parameter of type CommandSender or subclass to be used as a command", method.getName(), method.getDeclaringClass().getName()));
        }
        
        // Try to determine the sender type and make sure it is one
        Class<?> senderType;
        try {
            senderType = getSenderType(method.getGenericParameterTypes()[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Method %s in class %s %s", method.getName(), method.getDeclaringClass().getName(), e.getMessage()));
        }
        
        // Check return type
        if (!method.getReturnType().equals(Void.TYPE) && !method.getReturnType().equals(Void.class)) {
            throw new IllegalArgumentException(String.format("Method %s in class %s requires the return type to be void", method.getName(), method.getDeclaringClass().getName()));
        }
        
        // Determine priority
        CommandPriority priorityTag = method.getAnnotation(CommandPriority.class);
        int priority = 0;
        if (priorityTag != null) {
            priority = priorityTag.value();
        }
        
        // ok
        definitions.add(new CommandDefinition(method, tag.async(), priority, tag, senderType));
    }
    
    private Class<?> getSenderType(Type argType) throws IllegalArgumentException {
        // Try for just CommandSender or subclass
        if (argType instanceof Class<?>) {
            if (isCommandSender((Class<?>)argType)) {
                return (Class<?>)argType;
            } else {
                throw new IllegalArgumentException("requires the first parameter be of type CommandSender, a subclass of CommandSender, or a CommandContext to be used as a command");
            }
        // Try for CommandContext<CommandSender>
        } else if (argType instanceof ParameterizedType) {
            Class<?> rawType = (Class<?>)((ParameterizedType)argType).getRawType();
            if (CommandContext.class.isAssignableFrom(rawType)) {
                // Check the type parameter for CommandSender or subclass
                Type typeParam = ((ParameterizedType)argType).getActualTypeArguments()[0];
                if (typeParam instanceof Class<?>) {
                    if (isCommandSender((Class<?>)typeParam)) {
                        return ((Class<?>)typeParam);
                    } else {
                        throw new IllegalArgumentException(String.format("has invalid CommandContext type %s. The parameter must be CommandSender or a subclass.", ((Class<?>)typeParam).getName()));
                    }
                } else {
                    throw new IllegalArgumentException(String.format("has invalid CommandContext type %s. The parameter must be CommandSender or a subclass.", argType));
                }
            } else {
                throw new IllegalArgumentException("requires the first parameter be of type CommandSender, a subclass of CommandSender, or a CommandContext to be used as a command");
            }
        } else {
            throw new IllegalArgumentException("requires the first parameter be of type CommandSender, a subclass of CommandSender, or a CommandContext to be used as a command");
        }
    }
    
    public void build() throws IllegalArgumentException {
        // Sort by priority
        Collections.sort(definitions);
        
        compileParseTree();
        compileCommandInfo();
    }
        
    private void compileCommandInfo() {
        Set<String> aliases = Sets.newHashSet();
        List<String> usageList = Lists.newArrayList();
        
        boolean multiplePermissions = false;
        
        // Determine usage etc.
        for (CommandDefinition def : definitions) {
            if (!Strings.isNullOrEmpty(def.tag.usage())) {
                usageList.add(def.tag.usage().trim());
            }
            
            if (def.tag.aliases() != null) {
                for (String alias : def.tag.aliases()) {
                    aliases.add(alias);
                }
            }
            
            if (!multiplePermissions) {
                if (permission == null) {
                    permission = def.tag.permission();
                } else if (!permission.equalsIgnoreCase(def.tag.permission())) {
                    permission = null;
                    multiplePermissions = true;
                }
            }
            
            // Use first description that isnt empty
            if (description == null && !Strings.isNullOrEmpty(def.tag.description())) {
                description = def.tag.description();
            }
        }
        
        // Compile usage list into single string
        Collections.sort(usageList, String.CASE_INSENSITIVE_ORDER);
        fullUsage = Joiner.on('\n').join(usageList);
        
        // Store the rest
        this.aliases = Iterables.toArray(aliases, String.class);
        Arrays.sort(this.aliases, String.CASE_INSENSITIVE_ORDER);
    }
    
    private void compileParseTree() throws IllegalArgumentException {
        List<Variant> variants = Lists.newArrayListWithCapacity(definitions.size());
        for (CommandDefinition definition : definitions) {
            variants.add(Variant.fromMethod(variants.size(), definition.method));
        }
        
        ParseTreeBuilder builder = new ParseTreeBuilder(variants);
        ParseNode root = builder.build();
        
        parseTree = new ParseTree(root, variants);
    }
    
    /**
     * @return Returns the name of this command
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public String getName() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return name;
    }
    
    /**
     * @return Returns an array of aliases for this command.
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public String[] getAliases() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return aliases;
    }
    
    /**
     * @return Returns the description for this command if set
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public String getDescription() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return description;
    }
    
    /**
     * @return Returns the full usage string for this command
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public String getUsage() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return fullUsage;
    }
    
    /**
     * @return Returns the permission for this command. This may be null if there are
     *         multiple permissions
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public String getPermission() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return permission;
    }
    
    /**
     * @return Returns the ParseTree for this command
     * @throws IllegalStateException Thrown if build() has not run successfully before this
     */
    public ParseTree getParseTree() {
        Preconditions.checkState(parseTree != null, "build() not run or failed");
        return parseTree;
    }
    
    public List<CommandDefinition> getVariants() {
        return Collections.unmodifiableList(definitions);
    }
    
    protected abstract boolean isCommandSender(Class<?> clazz);
    
    public static class CommandDefinition implements Comparable<CommandDefinition> {
        public Method method;
        public int priority;
        public boolean async;
        public Command tag;
        public Class<?> senderType;
        public boolean useContext;
        
        public CommandDefinition(Method method, boolean async, int priority, Command tag, Class<?> senderType) {
            this.method = method;
            this.priority = priority;
            this.async = async;
            this.tag = tag;
            this.senderType = senderType;
            
            useContext = method.getParameterTypes()[0].equals(CommandContext.class);
        }

        @Override
        public int compareTo(CommandDefinition other) {
            return Integer.compare(other.priority, priority);
        }
        
        @Override
        public String toString() {
            return String.format("%d %s", priority, method);
        }
    }
}
