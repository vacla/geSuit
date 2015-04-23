package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class Result implements ByteStorable {
    private Type type;
    private String message;
    
    public Result(Type type, String message) {
        this.type = type;
        this.message = message;
    }
    
    protected Result() {}
    
    public Type getType() {
        return type;
    }
    
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
