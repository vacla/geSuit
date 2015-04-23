package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import net.cubespace.geSuit.core.util.NetworkUtils;

public class RemoteInvokeMessage extends BaseMessage implements LinkedMessage<Object> {
    public String name;
    public int methodId;
    public long invokeId;
    
    public Object[] parameters;
    public Object response;
    public Throwable error;

    public RemoteInvokeMessage() {}
    public RemoteInvokeMessage(String name, int method, long invoke, Object[] params) {
        this.name = name;
        this.methodId = method;
        this.invokeId = invoke;
        this.parameters = params;
    }
    
    public RemoteInvokeMessage(String name, long invoke, Object response, Throwable error) {
        this.name = name;
        this.invokeId = invoke;
        this.response = response;
        this.error = error;
    }
    
    @Override
    public boolean isReply() {
        return parameters == null;
    }
    
    @Override
    public Object getReply() throws ExecutionException {
        if (error != null) {
            throw new ExecutionException(error);
        }
        return response;
    }
    
    @Override
    public boolean isSource(LinkedMessage<?> message) {
        if (!(message instanceof RemoteInvokeMessage)) {
            return false;
        }
        
        return ((RemoteInvokeMessage)message).invokeId == invokeId && ((RemoteInvokeMessage)message).name.equals(name); 
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeLong(invokeId);

        if (!isReply()) {
            out.writeBoolean(false);
            out.writeInt(methodId);
            out.writeByte(parameters.length);
            for (Object param : parameters) {
                NetworkUtils.writeTyped(out, param);
            }
        } else {
            out.writeBoolean(true);
            if (error != null) {
                out.writeBoolean(false);
                NetworkUtils.writeObject(out, error);
            } else {
                out.writeBoolean(true);
                NetworkUtils.writeTyped(out, response);
            }
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        name = in.readUTF();
        invokeId = in.readLong();

        if (!in.readBoolean()) { // Request
            methodId = in.readInt();
            parameters = new Object[in.readByte()];
            for (int i = 0; i < parameters.length; ++i) {
                try {
                    parameters[i] = NetworkUtils.readTyped(in);
                } catch (ClassNotFoundException e) {
                    error = new IllegalStateException("Unable to deserialize parameter " + i + " value on remote:", e);
                    break;
                }
            }
        } else { // Response
            if (in.readBoolean()) { // Success
                error = null;
                try {
                    response = NetworkUtils.readTyped(in);
                } catch (ClassNotFoundException e) {
                    error = new IllegalStateException("Unable to deserialize return value:", e);
                }
            } else { // Error
                try {
                    error = (Throwable) NetworkUtils.readObject(in);
                } catch (ClassNotFoundException e) {
                    error = new IllegalStateException("Unable to deserialize error:", e);
                }
            }
        }
    }
}
