package net.cubespace.geSuit.core.objects;

import net.cubespace.geSuit.core.storage.SimpleStorable;

/**
 * Represents a position and direction somewhere on the network.
 * 
 * <p>null values for server or world are used to mean relative. These are often used in teleports</p>
 */
public class Location implements SimpleStorable {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    
    /**
     * Creates a new blank location
     */
    public Location() {
        this(null, null, 0, 0, 0, 0, 0);
    }
    
    /**
     * Creates a new location using just a position
     * @param server The name of the server or null
     * @param world The name of the world or null
     * @param x The x coord
     * @param y The y coord
     * @param z The z coord
     */
    public Location(String server, String world, double x, double y, double z) {
        this(server, world, x, y, z, 0, 0);
    }
    
    /**
     * Creates a new location using a position and direction
     * @param server The name of the server or null
     * @param world The name of the world or null
     * @param x The x coord
     * @param y The y coord
     * @param z The z coord
     * @param yaw The yaw
     * @param pitch The pitch
     */
    public Location(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    /**
     * @return Returns the name of the server this points to, or null if this is relative.
     */
    public String getServer() {
        return server;
    }
    
    /**
     * Sets the server this location point to
     * @param server The name or null
     */
    public void setServer(String server) {
        this.server = server;
    }
    
    /**
     * @return Returns the name of the world this points to, or null if this is relative
     */
    public String getWorld() {
        return world;
    }
    
    /**
     * Sets the world this location points to
     * @param world The name or null
     */
    public void setWorld(String world) {
        this.world = world;
    }
    
    /**
     * @return Returns the x coord
     */
    public double getX() {
        return x;
    }

    /**
     * @return Returns the y coord
     */
    public double getY() {
        return y;
    }

    /**
     * @return Returns the z coord
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the x coord
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y coord
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the z coord
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * @return Returns the yaw value
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * @return Returns the pitch value
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the yaw value
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * Sets the pitch value
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
    
    @Override
    public String toString() {
        return String.format("%.1f,%.1f,%.1f %.1f %.1f %s %s", x, y, z, yaw, pitch, world, server);
    }
    
    /**
     * @return A serialized version of this location which can be loaded again with {@link #fromSerialized(String)}
     */
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
    
    /**
     * Creates a Location from a serialized string made with {@link #toSerialized()}
     * @param serialized The serialized string
     * @return A location
     */
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
