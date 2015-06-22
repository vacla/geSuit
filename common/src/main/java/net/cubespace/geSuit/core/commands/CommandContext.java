package net.cubespace.geSuit.core.commands;

/**
 * <p>A CommandContext is a class you can use instead of a CommandSender
 * as the first argument on a command method. Using a CommandContext
 * allows you to handle parsing errors and provide useful feedback
 * to the caller.</p>
 * <p>This class is <b>optional</b> and does not need to be used if
 * you either do not need to change the displayed parse error output
 * or you cannot have a parse error (in the case of string arguments)</p> 
 *
 * @param <T> This <b>must</b> extend the appropriate CommandSender class
 *            of the platform the command is on. This type is used to
 *            filter the possible callers of the command. 
 */
public abstract class CommandContext<T> {
    private final T sender;
    private Command command;
    private String label;
    
    private Throwable error;
    private String errorMessage;
    private String errorArgumentValue;
    private int errorArgument;
    
    protected CommandContext(T sender, Command command, String label) {
        this.sender = sender;
        this.command = command;
        this.label = label;
    }
    
    protected CommandContext(T sender, Command command, String label, Throwable error, int argument, String argumentValue) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.error = error;
        this.errorMessage = error.getMessage();
        this.errorArgument = argument;
        this.errorArgumentValue = argumentValue;
    }
    
    /**
     * A convenience method to send a message to the command caller. 
     * Equivalent to {@code getSender().sendMessage(message)}
     * @param message The message to send
     */
    public abstract void sendMessage(String message);
    
    /**
     * @return Returns the caller of this command
     */
    public T getSender() {
        return sender;
    }
    
    /**
     * @return Returns the exact alias used to invoke this command
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * @return Returns the usage of this command as defined
     *         by the {@link Command} annotation
     */
    public String getUsage() {
        return command.usage();
    }
    
    /**
     * @return Returns the description of this command as defined
     *         by the {@link Command} annotation
     */
    public String getDescription() {
        return command.description();
    }
    
    /**
     * @return Returns true if there was a parse error. 
     * <p>If this is {@code true} <b>do not execute</b> the command.
     * Simply handle error messages and return</p>
     */
    public boolean isErrored() {
        return error != null;
    }
    
    /**
     * @return Returns the current error message when {@link #isErrored()} is {@code true}
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Sets the current error message to be displayed to the caller.
     * This has no effect if {@link #isErrored()} is {@code false}
     * @param message The message to display, or null to display command usage
     */
    public void setErrorMessage(String message) {
        errorMessage = message;
    }
    
    /**
     * @return Returns the index of the argument (not including the context argument)
     *         that the error occurred at. Only valid when {@link #isErrored()} is {@code true}
     */
    public int getErrorArg() {
        return errorArgument;
    }
    
    /**
     * @return Returns the raw input value that failed to be parsed.
     *         Only valid when {@link #isErrored()} is {@code true}
     */
    public String getErrorInput() {
        return errorArgumentValue;
    }
    
    /**
     * @return Returns the exception that was thrown during parsing.
     *         Only valid when {@link #isErrored()} is {@code true}
     */
    public Throwable getError() {
        return error;
    }
}
