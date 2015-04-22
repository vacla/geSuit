package net.cubespace.geSuit.core.objects;

public class Location {
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
        return String.format("Location: %.1f,%.1f,%.1f %.1f %.1f %s %s", x, y, z, yaw, pitch, world, server);
    }
    
    public String toSerialized() {
        return String.format("%s|%s|%.4f|%.4f|%.4f|%.4f|%.4f", server, world, x, y, z, yaw, pitch);
    }
    
    public static Location fromSerialized(String serialized) {
        String[] parts = serialized.split("\\|");
        
        return new Location(
            parts[0], 
            parts[1], 
            Double.parseDouble(parts[2]), 
            Double.parseDouble(parts[3]), 
            Double.parseDouble(parts[4]), 
            Float.parseFloat(parts[5]), 
            Float.parseFloat(parts[6])
            );
    }
}
