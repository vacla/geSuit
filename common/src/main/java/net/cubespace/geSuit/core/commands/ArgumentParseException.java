package net.cubespace.geSuit.core.commands;

public class ArgumentParseException extends RuntimeException implements Comparable<ArgumentParseException> {
    private static final long serialVersionUID = -6382620486403725353L;
    
    private ParseNode node;
    
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
    
    @Override
    public int compareTo(ArgumentParseException other) {
        return -Integer.compare(node.getArgumentIndex(), other.node.getArgumentIndex());
    }
}
