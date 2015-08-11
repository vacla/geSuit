package net.cubespace.geSuit.core.commands;

import java.util.Collections;
import java.util.List;

import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;

public class ArgumentParseException extends RuntimeException implements Comparable<ArgumentParseException> {
    private static final long serialVersionUID = -6382620486403725353L;
    
    private ParseNode node;
    private ParseResult partial = new ParseResult(null);
    private List<ParseNode> choices = Collections.emptyList();
    private String input;
    
    protected ArgumentParseException(ParseNode node, String input) {
        this.node = node;
        this.input = input;
    }
    
    protected ArgumentParseException(ParseNode node, String input, Throwable cause) {
        super(cause);
        this.node = node;
        this.input = input;
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
    
    /**
     * @return Returns the input value that could not be converted
     */
    public String getInput() {
        return input;
    }
    
    @Override
    public int compareTo(ArgumentParseException other) {
        return -Integer.compare(node.getArgumentIndex(), other.node.getArgumentIndex());
    }
}
