package net.cubespace.geSuit.core.objects;

import net.cubespace.geSuit.core.storage.SimpleStorable;

public class Location implements SimpleStorable {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    
    public Location() {
        this(null, null, 0, 0, 0, 0, 0);
    }
    
    public Location(String server, String world, double x, double y, double z) {
        this(server, world, x, y, z, 0, 0);
    }
    
    public Location(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public String getWorld() {
        return world;
    }
    
    public void setWorld(String world) {
        this.world = world;
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
    
    @Override
    public String toString() {
        return String.format("%.1f,%.1f,%.1f %.1f %.1f %s %s", x, y, z, yaw, pitch, world, server);
    }
    
    public String toSerialized() {
        return String.format("%s|%s|%.4f|%.4f|%.4f|%.4f|%.4f", (server == null ? "\0" : server), (world == null ? "\0" : world), x, y, z, yaw, pitch);
    }
    
    @Override
    public String save() {
        return toSerialized();
    }
    
    @Override
    public void load(String value) {
        String[] parts = value.split("\\|");
        server = parts[0];
        if (server.equals("\0")) {
            server = null;
        }
        world = parts[1];
        if (world.equals("\0")) {
            world = null;
        }
        x = Double.parseDouble(parts[2]); 
        y = Double.parseDouble(parts[3]); 
        z = Double.parseDouble(parts[4]); 
        yaw = Float.parseFloat(parts[5]);
        pitch = Float.parseFloat(parts[6]);
    }
    
    public static Location fromSerialized(String serialized) {
        String[] parts = serialized.split("\\|");
        
        return new Location(
            (parts[0].equals("\0") ? null : parts[0]), 
            (parts[1].equals("\0") ? null : parts[1]), 
            Double.parseDouble(parts[2]), 
            Double.parseDouble(parts[3]), 
            Double.parseDouble(parts[4]), 
            Float.parseFloat(parts[5]), 
            Float.parseFloat(parts[6])
            );
    }
}
