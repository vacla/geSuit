package net.cubespace.geSuit.core.commands;

/**
 * This exception is thrown when the input match's
 * syntactically but the content is rejected by parsers.
 * This will happen if the argument count matches, but a
 * parser cannot convert one of the arguments.
 */
public class CommandInterpretException extends ArgumentParseException {
    private static final long serialVersionUID = 3599496059237841441L;
    
    private String input;
    
    public CommandInterpretException(ParseNode currentNode, String input, Throwable cause) {
        super(currentNode, cause);
        this.input = input;
    }
    
    /**
     * @return Returns the input value that could not be converted
     */
    public String getInput() {
        return input;
    }
}
