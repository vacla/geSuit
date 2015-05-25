package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;

/**
 * A simple container for holding a status and response.
 * This is used for remote command execution
 */
public class Result implements ByteStorable {
    private Type type;
    private String message;
    
    public Result(Type type, String message) {
        this.type = type;
        this.message = message;
    }
    
    protected Result() {}
    
    /**
     * @return Returns whether this was a successful execution
     */
    public Type getType() {
        return type;
    }
    
    /**
     * @return Returns a message to display or null if there isnt one
     */
    public String getMessage() {
        return message;
    }
    
    @Override
    public void save(DataOutput out) throws IOException {
        NetworkUtils.writeEnum(out, type);
        if (message != null) {
            out.writeBoolean(true);
            out.writeUTF(message);
        } else {
            out.writeBoolean(false);
        }
    }
    
    @Override
    public void load(DataInput in) throws IOException {
        type = NetworkUtils.readEnum(in, Type.class);
        if (in.readBoolean()) {
            message = in.readUTF();
        }
    }
    
    public enum Type {
        Success,
        Fail
    }
}
