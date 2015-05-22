package net.cubespace.geSuit.core.commands;

public class ArgumentParseException extends RuntimeException implements Comparable<ArgumentParseException> {
    private static final long serialVersionUID = -6382620486403725353L;
    
    private String argumentValue;
    private int argumentIndex;
    private ParseNode node;
    private String reason;
    
    public ArgumentParseException(ParseNode node, int argumentIndex, String argumentValue, String reason) {
        this.node = node;
        this.argumentIndex = argumentIndex;
        this.argumentValue = argumentValue;
        this.reason = reason;
    }
    
    public ParseNode getNode() {
        return node;
    }
    
    public int getArgument() {
        return argumentIndex;
    }
    
    public String getValue() {
        return argumentValue;
    }
    
    public String getReason() {
        return reason;
    }

    @Override
    public int compareTo(ArgumentParseException other) {
        return -Integer.compare(argumentIndex, other.argumentIndex);
    }
}
