package net.cubespace.geSuit.core;

public class GlobalServer {
    private String name;
    private int id;
    
    public GlobalServer(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
}
