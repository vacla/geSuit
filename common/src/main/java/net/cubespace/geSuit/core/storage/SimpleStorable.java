package net.cubespace.geSuit.core.storage;

/**
 * SimpleStorable allows objects to be saved and loaded with strings. 
 */
public interface SimpleStorable {
    /**
     * @return Returns a string version of this class that can be loaded with {@link #load(String)}
     */
    public String save();
    /**
     * Loads the string version of this class as created by {@link #save()}
     * @param value The string value
     */
    public void load(String value);
}
