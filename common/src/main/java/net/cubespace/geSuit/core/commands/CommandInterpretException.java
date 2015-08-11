package net.cubespace.geSuit.core.commands;

/**
 * This exception is thrown when the input match's
 * syntactically but the content is rejected by parsers.
 * This will happen if the argument count matches, but a
 * parser cannot convert one of the arguments.
 */
public class CommandInterpretException extends ArgumentParseException {
    private static final long serialVersionUID = 3599496059237841441L;
    
    public CommandInterpretException(ParseNode currentNode, String input, Throwable cause) {
        super(currentNode, input, cause);
    }
}
