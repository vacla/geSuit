package net.cubespace.geSuit.core.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A simple interface that indicates a type is able to be stored and loaded from a stream of some kind.
 */
public interface ByteStorable {
    /**
     * When overridden, saves this class into the DataOutput provided
     * @param out The DO to write to 
     * @throws IOException Thrown if writing fails 
     */
    public void save(DataOutput out) throws IOException;
    /**
     * When overridden, reads this class from the DataInput provided.
     * @param in The DI to read from
     * @throws IOException Thrown if reading fails
     */
    public void load(DataInput in) throws IOException;
}
