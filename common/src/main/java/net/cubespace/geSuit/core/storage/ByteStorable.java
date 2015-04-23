package net.cubespace.geSuit.core.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ByteStorable {
    public void save(DataOutput out) throws IOException;
    public void load(DataInput in) throws IOException;
}
