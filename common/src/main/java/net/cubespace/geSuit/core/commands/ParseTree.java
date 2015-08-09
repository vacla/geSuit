package net.cubespace.geSuit.core.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;

class ParseTree {
    private ParseNode root;
    private List<Variant> variants;

    public ParseTree(ParseNode root, List<Variant> variants) {
        this.root = root;
        this.variants = variants;
    }
    
    public ParseResult parse(String[] arguments) throws ArgumentParseException {
        ParseResult result = recurseChildren(root, arguments);
        result.ensureParameters(getVariant(result.variant).getParameters().size() - 1);
        return result;
    }
    
    private ParseResult parseNode(ParseNode node, String[] arguments) throws ArgumentParseException {
        if (node.isTerminal()) {
            if (!node.getParent().isVarArgs() && arguments.length > node.getArgumentIndex()) {
                throw new CommandSyntaxException(node, true);
            }
            
            return new ParseResult(node.getVariant());
        }
        
        if (arguments.length <= node.getArgumentIndex()) {
            throw new CommandSyntaxException(node, false);
        }
        
        String argument = arguments[node.getArgumentIndex()];
        if (node.isVarArgs()) {
            argument = Joiner.on(' ').join(Arrays.copyOfRange(arguments, node.getArgumentIndex(), arguments.length));
        }
        
        Object value = null;
        try {
            value = node.parse(argument);
        } catch (IllegalArgumentException e) {
            throw new CommandInterpretException(node, argument, e);
        }
        
        // Now combine with children
        try {
            ParseResult result = recurseChildren(node, arguments);
            result.setParameter(node.getArgumentIndex(), value, argument);
            return result;
        } catch (ArgumentParseException e) {
            e.getPartialResult().setParameter(node.getArgumentIndex(), value, argument);
            throw e;
        }
    }
    
    private ParseResult recurseChildren(ParseNode node, String[] arguments) throws ArgumentParseException {
        List<ArgumentParseException> caughtErrors = Lists.newArrayList();
        boolean interpretException = false;
        for (ParseNode child : node.getChildren()) {
            // If there has been an error in parsing a value
            // Do not allow terminal nodes to be parsed
            if (child.isTerminal() && interpretException) {
                continue;
            }
            try {
                return parseNode(child, arguments);
            } catch (CommandInterpretException e) {
                interpretException = true;
                e.setChoices(node.getChildren());
                caughtErrors.add(e);
            } catch (ArgumentParseException e) {
                e.setChoices(node.getChildren());
                caughtErrors.add(e);
            }
        }
        
        // Use the error that went the deepest
        Collections.sort(caughtErrors);
        throw caughtErrors.get(0);
    }
    
    public Invokable<Object, Void> getVariant(int index) {
        return variants.get(index).method;
    }
    
    @Override
    public String toString() {
        return root.toString();
    }
    
    public static class ParseResult {
        public int variant;
        public List<Object> parameters;
        public List<String> input;
        
        public ParseResult(int variant) {
            this.variant = variant;
            parameters = Lists.newArrayList();
            input = Lists.newArrayList();
        }
        
        public void setParameter(int index, Object param, String raw) {
            while (index >= parameters.size()) {
                parameters.add(null);
                input.add(null);
            }
            
            parameters.set(index, param);
            input.set(index, raw);
        }
        
        public void ensureParameters(int count) {
            while (count > parameters.size()) {
                parameters.add(null);
            }
        }
    }
}
