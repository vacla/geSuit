package net.cubespace.geSuit.core.commands;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cubespace.geSuit.core.storage.DataConversion;

import com.google.common.base.Converter;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;

class ParseTree {
    private ParseNode root;
    private Set<Variant> variants;
    
    public ParseTree(List<Method> methods) {
        variants = Sets.newLinkedHashSet();
        for (int i = 0; i < methods.size(); ++i) {
            variants.add(new Variant(i, methods.get(i)));
        }
        
        root = new ParseNode(-1, -1, null, false);
    }
    
    public void build() throws IllegalArgumentException {
        // First parameter is the command sender, so ignore it
        buildLevel(root, variants, 1);
    }
    
    private void buildLevel(ParseNode parent, Set<Variant> active, int index) {
        // Used to group the same types together
        Map<TypeToken<?>, ParseNode> typeMap = Maps.newHashMap();
        // Stores what will be next after each node
        ListMultimap<ParseNode, Marker> markers = ArrayListMultimap.create();
        
        // Create this levels nodes
        for (Variant var : active) {
            buildVariant(var, parent, index, typeMap, markers);
        }
        
        // Recurse into deeper levels
        for (ParseNode nextNode : markers.keySet()) {
            for (Marker marker : markers.get(nextNode)) {
                buildLevel(nextNode, marker.variants, marker.index);
            }
        }
    }
        
    private void buildVariant(Variant var, ParseNode parent, int index, Map<TypeToken<?>, ParseNode> typeMap, ListMultimap<ParseNode, Marker> markers) {
        List<Parameter> parameters = var.method.getParameters();
        
        if (index >= parameters.size()) {
            // This variant is done
            parent.addChild(new ParseNode(var.id, index-1));
        } else {
            Parameter current = parameters.get(index);
            ParseNode node = typeMap.get(current.getType());
            
            boolean optional = current.isAnnotationPresent(Optional.class);
            boolean varargs = current.isAnnotationPresent(Varargs.class);
            
            // Need to create the node and get the converter
            // Varargs will always be its own node
            if (node == null || varargs) {
                node = makeNode(var, index, current.getType(), varargs);
                parent.addChild(node);
                
                if (!varargs)
                    typeMap.put(current.getType(), node);
            }
            
            // Make note of what to do at this node
            List<Marker> existingMarkers = markers.get(node);
            if (existingMarkers.isEmpty()) {
                existingMarkers.add(new Marker(var, index+1));
            } else {
                // Only add to the one at the same level
                for (Marker marker : existingMarkers) {
                    if (marker.index == index + 1) {
                        marker.variants.add(var);
                    }
                }
            }
            
            // Optional needs to add the next node after this one as an alternate path
            if (optional) {
                buildVariant(var, parent, index+1, typeMap, markers);
            }
        }
    }
    
    private ParseNode makeNode(Variant variant, int index, TypeToken<?> type, boolean varargs) {
        Converter<String, ?> converter = DataConversion.getConverter(type.getRawType());
        if (converter == null) {
            throw new IllegalArgumentException("Unable to register method " +  variant.method + " as command. There is no converter registered for type " + type.toString());
        }
        
        return new ParseNode(variant.id, index-1, converter, varargs);
    }
    
    public ParseResult parse(String[] arguments) throws ArgumentParseException {
        // During parsing we must start at the root, then proceed to each child. At each node, if a type, we must try to parse the current argument as that object. If it succeeds, we recurse and start the process with its children
        // If a parsing a node fails, move back up the parse tree and try the next available node
        // If no path makes it to a leaf, then the syntax is wrong, we should record the one which made it the furthest and try to suggest a correct value. or we can just show the usage
        //debugPrint(System.out);
        ParseResult result = recurseChildren(root, arguments);
        result.ensureParameters(getVariant(result.variant).getParameters().size() - 1);
        return result;
    }
    
    private ParseResult parseNode(ParseNode node, String[] arguments) throws ArgumentParseException {
        //DebugPrintStream.systemOut.print(Strings.repeat(" ", node.getDepth()*2) + node.getDebugName() + ": ");
        if (node.isTerminal()) {
            if (arguments.length > node.getArgumentIndex()) {
                //DebugPrintStream.systemOut.println("#" + arguments[node.getArgumentIndex()]);
                throw new ArgumentParseException(node, node.getArgumentIndex(), arguments[node.getArgumentIndex()], "More input");
            }
            //DebugPrintStream.systemOut.println("*fin*");
            return new ParseResult(node.getVariant());
        }
        
        if (arguments.length <= node.getArgumentIndex()) {
            //DebugPrintStream.systemOut.println("*eol*");
            throw new ArgumentParseException(node, node.getArgumentIndex(), null, "Premature end");
        }
        
        String argument = arguments[node.getArgumentIndex()];
        if (node.isVarArgs()) {
            argument = Joiner.on(' ').join(Arrays.copyOfRange(arguments, node.getArgumentIndex(), arguments.length));
        }
        
        //DebugPrintStream.systemOut.print("'" + argument + "': ");
        
        Object value = null;
        try {
            value = node.parse(argument);
            //DebugPrintStream.systemOut.println("*ok*");
        } catch (IllegalArgumentException e) {
            //DebugPrintStream.systemOut.println("*err* " + e.getMessage());
            throw new ArgumentParseException(node, node.getArgumentIndex(), argument, "Wrong value");
        }
        
        // Now combine with children
        ParseResult result = recurseChildren(node, arguments);
        result.setParameter(node.getArgumentIndex(), value);
        
        return result;
    }
    
    private ParseResult recurseChildren(ParseNode node, String[] arguments) throws ArgumentParseException {
        List<ArgumentParseException> caughtErrors = Lists.newArrayList();
        for (ParseNode child : node.getChildren()) {
            try {
                return parseNode(child, arguments);
            } catch (ArgumentParseException e) {
                caughtErrors.add(e);
            }
        }
        
        // Use the error that went the deepest
        Collections.sort(caughtErrors);
        throw caughtErrors.get(0);
    }
    
    public Invokable<Object, Void> getVariant(int index) {
        for (Variant var : variants) {
            if (var.id == index) {
                return var.method;
            }
        }
        throw new IndexOutOfBoundsException();
    }
    
    @Override
    public String toString() {
        return root.toString();
    }
    
    public void debugPrint(PrintStream out) {
        out.println("Variants:");
        for (Variant var : variants) {
            out.println(" " + var.id + ": " + var.method.toString());
        }
        out.println("Tree:");
        root.debugPrint(out, 0);
    }
    
    
    
    private static class Variant {
        public int id;
        public Invokable<Object, Void> method;
        
        @SuppressWarnings("unchecked")
        public Variant(int id, Method method) {
            this.id = id;
            if (method.getReturnType().equals(Void.class)) {
                this.method = (Invokable<Object, Void>)Invokable.from(method).returning(Void.class);
            } else {
                this.method = (Invokable<Object, Void>)Invokable.from(method).returning(Void.TYPE);
            }
        }
        
        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }
    
    private static class Marker {
        public Set<Variant> variants;
        public int index;
        
        public Marker(Variant variant, int index) {
            this.variants = Sets.newLinkedHashSet();
            this.variants.add(variant);
            this.index = index;
        }
    }
    
    public static class ParseResult {
        public int variant;
        public List<Object> parameters;
        
        public ParseResult(int variant) {
            this.variant = variant;
            parameters = Lists.newArrayList();
        }
        
        public void setParameter(int index, Object param) {
            while (index >= parameters.size()) {
                parameters.add(null);
            }
            
            parameters.set(index, param);
        }
        
        public void ensureParameters(int count) {
            while (count > parameters.size()) {
                parameters.add(null);
            }
        }
    }
}
