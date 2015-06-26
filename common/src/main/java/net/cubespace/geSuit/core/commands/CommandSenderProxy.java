package net.cubespace.geSuit.core.commands;

interface CommandSenderProxy {
    public Object getSender();
    
    public boolean isPlayer();
    
    public void sendMessage(String message);
    
    public boolean hasPermission(String permission);
    
    public CommandContext<?> asContext(Command tag, String label);

    public CommandContext<?> asErrorContext(Command tag, String label, Throwable cause, int argumentIndex, String input);
}
