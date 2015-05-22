package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.serialization.Serialization;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class RemoteInvokeMessage implements LinkedMessage<Object> {
    public String name;
    public int methodId;
    public long invokeId;
    
    public Object[] parameters;
    public Object response;
    public Throwable error;

    public RemoteInvokeMessage() {}
    public RemoteInvokeMessage(String name, int methodId, long invoke, Object[] params) {
        this.name = name;
        this.methodId = methodId;
        this.invokeId = invoke;
        this.parameters = params;
    }
    
    public RemoteInvokeMessage(String name, int methodId, long invoke, Object response, Throwable error) {
        this.name = name;
        this.methodId = methodId;
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
    
    public void write(Method method, DataOutput out) throws IOException {
        out.writeLong(invokeId);

        if (!isReply()) {
            out.writeBoolean(false);
            out.writeByte(parameters.length);
            for (int i = 0; i < parameters.length; ++i) {
                Serialization.serialize(parameters[i], (TypeToken<Object>)TypeToken.of(method.getGenericParameterTypes()[i]), out);
            }
        } else {
            out.writeBoolean(true);
            if (error != null) {
                out.writeBoolean(false);
                NetworkUtils.writeObject(out, error);
            } else {
                out.writeBoolean(true);
                Serialization.serialize(response, (TypeToken<Object>)TypeToken.of(method.getGenericReturnType()), out);
            }
        }
    }

    public void read(Method method, DataInput in) throws IOException {
        invokeId = in.readLong();

        if (!in.readBoolean()) { // Request
            parameters = new Object[in.readByte()];
            for (int i = 0; i < parameters.length; ++i) {
                parameters[i] = Serialization.deserialize((TypeToken<Object>)TypeToken.of(method.getGenericParameterTypes()[i]), in);
            }
        } else { // Response
            if (in.readBoolean()) { // Success
                error = null;
                response = Serialization.deserialize((TypeToken<Object>)TypeToken.of(method.getGenericReturnType()), in);
            } else { // Error
                try {
                    error = (Throwable) NetworkUtils.readObject(in);
                } catch (ClassNotFoundException e) {
                    error = new IllegalStateException("Unable to deserialize error:", e);
                }
            }
        }
    }
    
    @Override
    public String toString() {
        if (isReply()) {
            return String.format("InvokeReply: %s %d #%d response:%s err:%s", name, methodId, invokeId, response, error);
        } else {
            return String.format("RemoteInvoke: %s %d #%d params:%s", name, methodId, invokeId, Arrays.toString(parameters));
        }
    }
}
