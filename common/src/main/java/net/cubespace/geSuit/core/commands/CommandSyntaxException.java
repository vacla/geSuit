package net.cubespace.geSuit.core.commands;

/**
 * This exception is thrown when the input given cannot
 * match any variant of the command. This is usually due
 * to an argument count mismatch.
 */
public class CommandSyntaxException extends ArgumentParseException {
    private static final long serialVersionUID = -8648430566494551144L;

    private boolean hasMoreInput;
    
    public CommandSyntaxException(ParseNode currentNode, boolean hasMore) {
        super(currentNode);
        
        hasMoreInput = hasMore;
    }
    
    public boolean hasMoreInput() {
        return hasMoreInput;
    }
}
