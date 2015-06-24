package net.cubespace.geSuit.core.commands;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.cubespace.geSuit.core.storage.DataConversion;

import com.google.common.base.Converter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;

class ParseTreeBuilder {
    private List<Variant> variants;
    
    public ParseTreeBuilder(List<Variant> variants) {
        
        this.variants = variants;
    }
    
    public ParseNode build() throws IllegalArgumentException {
        ParseNode root = new ParseNode(-1, -1, null, false);
        // First parameter is the command sender, so ignore it
        buildLevel(root, variants, 1);
        
        return root;
    }
    
    private void buildLevel(ParseNode parent, List<Variant> active, int index) {
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
    
    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }
    
    public static class Variant {
        public int id;
        public Invokable<Object, Void> method;
        
        public Variant(int id, Invokable<Object, Void> method) {
            this.id = id;
            this.method = method;
        }
        
        @Override
        public String toString() {
            return String.valueOf(id);
        }
        
        @SuppressWarnings("unchecked")
        public static Variant fromMethod(int id, Method method) {
            if (method.getReturnType().equals(Void.class)) {
                return new Variant(id, (Invokable<Object, Void>)Invokable.from(method).returning(Void.class));
            } else {
                return new Variant(id, (Invokable<Object, Void>)Invokable.from(method).returning(Void.TYPE));
            }
        }
    }
    
    private static class Marker {
        public List<Variant> variants;
        public int index;
        
        public Marker(Variant variant, int index) {
            this.variants = Lists.newArrayList();
            this.variants.add(variant);
            this.index = index;
        }
    }
}
