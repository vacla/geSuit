package net.cubespace.geSuit.objects;

public class Home {
    public GSPlayer owner;
    public String name;
    public Location loc;

    public Home(GSPlayer owner, String name, Location loc) {
        this.owner = owner;
        this.name = name;
        this.loc = loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }
}
