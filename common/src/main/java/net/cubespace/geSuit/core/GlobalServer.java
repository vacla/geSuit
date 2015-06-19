package net.cubespace.geSuit.core;

/**
 * Represents a server on the network.
 * This can either represent a BungeeCord or
 * Spigot server.
 */
public class GlobalServer {
    private String name;
    private int id;
    
    public GlobalServer(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    /**
     * @return Returns the name of this server as 
     * defined by BungeeCord
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Returns the ID of this server used for
     * sending messages to it
     */
    public int getId() {
        return id;
    }
}
