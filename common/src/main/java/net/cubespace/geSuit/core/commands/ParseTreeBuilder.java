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
    private Map<TypeToken<?>, Map<Long, ParseNode>> typeMap;
    
    public ParseTreeBuilder(List<Variant> variants) {
        this.variants = variants;
        typeMap = Maps.newHashMap();
    }
    
    public ParseNode build() throws IllegalArgumentException {
        ParseNode root = ParseNode.newRootNode();
        List<Marker> markers = Lists.newArrayList();
        for (Variant variant : variants) {
            markers.add(new Marker(variant, 0, 0));
        }
        
        buildMarkers(root, markers);
        
        return root;
    }
    
    /*
     * Converts all the markers provided into parse nodes as needed
     */
    private void buildMarkers(ParseNode parent, List<Marker> markers) {
        // Stores what will be next after each node
        ListMultimap<ParseNode, Marker> childMarkers = ArrayListMultimap.create();
        
        // Create this levels nodes
        for (Marker marker : markers) {
            buildMarker(marker, parent, childMarkers);
        }
        
        // Recurse into deeper levels
        for (ParseNode nextNode : childMarkers.keySet()) {
            buildMarkers(nextNode, childMarkers.get(nextNode));
        }
    }
        
    private void buildMarker(Marker marker, ParseNode parent, ListMultimap<ParseNode, Marker> childMarkers) {
        List<Parameter> parameters = marker.variant.method.getParameters();
        
        if (marker.argumentIndex >= parameters.size()-1) {
            // This variant is done
            parent.addChild(ParseNode.newTerminalNode(marker.variant.id, marker.argumentIndex, marker.inputIndex));
        } else {
            Parameter current = parameters.get(marker.argumentIndex+1);
            
            boolean optional = current.isAnnotationPresent(Optional.class);
            boolean varargs = current.isAnnotationPresent(Varargs.class);
            
            ParseNode node = getParseNode(marker, parent, current, varargs);
            
            childMarkers.put(node, new Marker(marker.variant, marker.argumentIndex+1, marker.inputIndex+1));
            
            // Optional needs to add the next node after this one as an alternate path
            if (optional) {
                buildMarker(new Marker(marker.variant, marker.argumentIndex+1, marker.inputIndex), parent, childMarkers);
            }
        }
    }
    
    private ParseNode makeNode(Marker marker, TypeToken<?> type, boolean varargs) {
        Converter<String, ?> converter = DataConversion.getConverter(type.getRawType());
        if (converter == null) {
            throw new IllegalArgumentException("Unable to register method " +  marker.variant.method + " as command. There is no converter registered for type " + type.toString());
        }
        
        return ParseNode.newTransformNode(marker.variant.id, marker.argumentIndex, marker.inputIndex, converter, varargs);
    }
    
    private ParseNode getParseNode(Marker marker, ParseNode parent, Parameter param, boolean varArgs) {
        Map<Long, ParseNode> nodes = typeMap.get(param.getType());
        if (nodes == null) {
            nodes = Maps.newHashMap();
            typeMap.put(param.getType(), nodes);
        }
        
        ParseNode node;
        // Varargs do not use an existing node
        if (!varArgs) {
            node = nodes.get((long)marker.argumentIndex << 32L | (long)marker.inputIndex);
            if (node != null) {
                return node;
            }
        }
        
        // Create a new one
        node = makeNode(marker, param.getType(), varArgs);
        parent.addChild(node);
        
        // Store it
        if (!varArgs) {
            nodes.put((long)marker.argumentIndex << 32L | (long)marker.inputIndex, node);
        }
        
        return node;
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
    
    /**
     * Marks a position in the parse tree.
     * This is used to mark the next path to build
     */
    private static class Marker {
        public final Variant variant;
        public final int argumentIndex;
        public final int inputIndex;
        
        public Marker(Variant variant, int argumentIndex, int inputIndex) {
            this.variant = variant;
            this.argumentIndex = argumentIndex;
            this.inputIndex = inputIndex;
        }
        
        @Override
        public String toString() {
            return String.format("Marker: %d:%d %s", argumentIndex, inputIndex, variant);
        }
    }
}
