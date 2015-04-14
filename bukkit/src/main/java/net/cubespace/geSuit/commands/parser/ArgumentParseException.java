package net.cubespace.geSuit.commands.parser;

public class ArgumentParseException extends RuntimeException implements Comparable<ArgumentParseException> {
    private static final long serialVersionUID = -6382620486403725353L;
    
    private String argumentValue;
    private int argumentIndex;
    private ParseNode node;
    
    public ArgumentParseException(ParseNode node, int argumentIndex, String argumentValue) {
        this.node = node;
        this.argumentIndex = argumentIndex;
        this.argumentValue = argumentValue;
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

    @Override
    public int compareTo(ArgumentParseException other) {
        return -Integer.compare(argumentIndex, other.argumentIndex);
    }
}
