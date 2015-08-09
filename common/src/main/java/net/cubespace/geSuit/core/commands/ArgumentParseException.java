package net.cubespace.geSuit.core.commands;

import java.util.Collections;
import java.util.List;

import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;

public class ArgumentParseException extends RuntimeException implements Comparable<ArgumentParseException> {
    private static final long serialVersionUID = -6382620486403725353L;
    
    private ParseNode node;
    private ParseResult partial = new ParseResult(-1);
    private List<ParseNode> choices = Collections.emptyList();
    
    protected ArgumentParseException(ParseNode node) {
        this.node = node;
    }
    
    protected ArgumentParseException(ParseNode node, Throwable cause) {
        super(cause);
        this.node = node;
    }
    
    public ParseNode getNode() {
        return node;
    }
    
    public ParseResult getPartialResult() {
        return partial;
    }
    
    public List<ParseNode> getChoices() {
        return choices;
    }
    
    void setChoices(List<ParseNode> choices) {
        if (this.choices.isEmpty()) {
            this.choices = Collections.unmodifiableList(choices);
        }
    }
    
    @Override
    public int compareTo(ArgumentParseException other) {
        return -Integer.compare(node.getArgumentIndex(), other.node.getArgumentIndex());
    }
}
