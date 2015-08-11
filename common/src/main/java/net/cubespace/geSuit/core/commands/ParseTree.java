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
        result.ensureParameters(getVariant(result.node.getVariant()).getParameters().size() - 1);
        return result;
    }
    
    private ParseResult parseNode(ParseNode node, String[] arguments) throws ArgumentParseException {
        if (node.isTerminal()) {
            if (!node.getParent().isVarArgs() && arguments.length > node.getInputIndex()) {
                throw new CommandSyntaxException(node, arguments[node.getInputIndex()], true);
            }
            
            return new ParseResult(node);
        }
        
        if (arguments.length <= node.getInputIndex()) {
            throw new CommandSyntaxException(node, (arguments.length > 0 ? arguments[arguments.length-1] : ""), false);
        }
        
        String argument = arguments[node.getInputIndex()];
        if (node.isVarArgs()) {
            argument = Joiner.on(' ').join(Arrays.copyOfRange(arguments, node.getInputIndex(), arguments.length));
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
                ParseResult result = parseNode(child, arguments);
                // Fill all other options for tab complete use
                for (ParseNode child2 : node.getChildren()) {
                    if (child2.getInputIndex() == result.node.getInputIndex()-1) {
                        result.options.add(child2);
                    }
                }
                
                return result;
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
        public List<Object> parameters;
        public List<String> input;
        public List<ParseNode> options;
        public ParseNode node;
        
        public ParseResult(ParseNode node) {
            this.node = node;
            parameters = Lists.newArrayList();
            input = Lists.newArrayList();
            options = Lists.newArrayList();
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
