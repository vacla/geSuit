package net.cubespace.geSuit.core.remote;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelCodec;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.messages.RemoteInvokeMessage;
import net.cubespace.geSuit.core.serialization.Serialization;

/**
 * Remotes allow you to execute code on other servers with the ease of executing local code.
 * 
 * <p><b>TEMINOLOGY:</b> The execution side is the side that holds the actual functionality of the interface. 
 * The calling side is the side that holds a proxy</p> 
 * 
 * <p>Remotes require an interface class which defines all methods the remote will make available. 
 * On the execution side, there must be a class that implemented that interface. Both are 
 * provided with a call to {@link #registerRemote(String, Class, Object)}. </p>
 * 
 * <p>The calling side requires access to that interface, so you will need to place it in a 
 * common location, or provide a copy on both sides. To use it, you must first register your interest in
 * using that remote with {@link #registerInterest(String, Class)}. Once you have registered your interest,
 * you can call {@link #getRemote(Class)} to get a version of that interface you can execute.</p>
 * 
 * <p>Behind the scenes, the calling side uses a reflection proxy to enable you to use the interface instead
 * of having to use untyped methods.</p>
 */
public class RemoteManager implements ChannelDataReceiver<RemoteInvokeMessage> {
    private Channel<RemoteInvokeMessage> channel;
    private MessageWaiter waiter;
    private Map<Class<?>, RemoteInterface<?>> remotesByClass;
    private Map<String, RemoteInterface<?>> remotesByName;
    
    public RemoteManager(ChannelManager manager) {
        channel = manager.createChannel("remote", RemoteInvokeMessage.class);
        channel.setCodec(new MessageCodec());
        channel.addReceiver(this);
        
        waiter = new MessageWaiter();
        
        remotesByClass = Maps.newHashMap();
        remotesByName = Maps.newHashMap();
    }
    
    /**
     * Defines a new remote that is usable on other servers.
     * <h2>Interface requirements:</h2>
     * <ul>
     *   <li>The {@code interfaceClass} must be a java interface, not an abstract class or anything else</li>
     *   <li>Every return type and parameter of every method must be serializable through the {@link Serialization} system</li>
     *   <li>ALL exceptions you intend to throw must be declared with {@code throws}. Any undeclared thrown exceptions will be logged</li>
     * </ul>
     * @param name The name of this remote. This name must be unique.
     * @param interfaceClass The interface that this remote is based on
     * @param implementation The execution side implementation of the interface.
     * @throws IllegalArgumentException Thrown if the remote is already registered (either by name or by interface)
     */
    public <T> void registerRemote(String name, Class<T> interfaceClass, T implementation) {
        Preconditions.checkArgument(!remotesByClass.containsKey(interfaceClass), "This type is already registered");
        Preconditions.checkArgument(!remotesByName.containsKey(name), "This name is already registered");
        
        checkValidity(interfaceClass);
        
        RemoteInterface<T> handler = new RemoteInterface<T>(interfaceClass, implementation);
        remotesByClass.put(interfaceClass, handler);
        remotesByName.put(name, handler);
    }
    
    /**
     * Registers your interest in this remote. It is required that you do this before you can use the remote.
     * This method sets up the proxy needed to be able to forward calls to the execution side.
     * @param name The name of the remote. This must be the same as defined on the execution side.
     * @param interfaceClass The interface class the remote is based on. This interface MUST contain the exact same methods as the 
     *                       execution side but does not need to have the same name or package.
     */
    public <T> void registerInterest(String name, Class<T> interfaceClass) {
        // TODO: Dont throw an exception if interest is already registered, just ignore
        Preconditions.checkArgument(!remotesByClass.containsKey(interfaceClass), "This type is already registered");
        Preconditions.checkArgument(!remotesByName.containsKey(name), "This name is already registered");
        
        checkValidity(interfaceClass);
        
        RemoteInvocationHandler invocHandler = new RemoteInvocationHandler(name, channel, waiter);
        RemoteInterface<T> remoteHandler = new RemoteInterface<T>(interfaceClass, invocHandler);
        
        remotesByClass.put(interfaceClass, remoteHandler);
        remotesByName.put(name, remoteHandler);
    }
    
    /**
     * Retrieves an executable version of the {@code interfaceClass}. 
     * The interface must have already been registered with {@link #registerInterest(String, Class)} 
     * or {@link #registerRemote(String, Class, Object)} before this method can be used.
     * <h2>Calling remotes</h2>
     * <p>Methods in remotes can be called like any other java method but the following must be considered:
     * <ul>
     *   <li>Method calls can timeout with {@link RemoteTimeoutException} after a maximum of 5 seconds</li>
     *   <li>The calls are blocking so don't use them on a thread you can't afford to wait up to 5 seconds on.</li>
     *   <li>If the remote's interface does not match on both the execution and calling side, {@link InvalidRemoteException} can be thrown</li>
     * </ul>
     * @param interfaceClass The registered interface class
     * @return An executable version of {@code interfaceClass}
     */
    public <T> T getRemote(Class<T> interfaceClass) {
        @SuppressWarnings("unchecked") // The types are forced to match on register
        RemoteInterface<T> remoteHandler = (RemoteInterface<T>)remotesByClass.get(interfaceClass);
        
        if (remoteHandler == null) {
            return null;
        } else {
            return remoteHandler.get();
        }
    }
    
    @Override
    public void onDataReceive(Channel<RemoteInvokeMessage> channel, RemoteInvokeMessage value, int sourceId, boolean isBroadcast) {
        RemoteInvokeMessage message = (RemoteInvokeMessage)value;
        
        if (message.isReply()) {
            waiter.checkMessage(message);
        } else {
            onInvoke(message, sourceId);
        }
    }
    
    private void onInvoke(RemoteInvokeMessage message, int sourceId) {
        RemoteInterface<?> remote = remotesByName.get(message.name);
        if (remote == null || remote.isRemote()) {
            return;
        }
        
        RemoteInvokeMessage reply;
        try {
            Object returnValue = remote.invoke(message.methodId, message.parameters);
            reply = new RemoteInvokeMessage(message.name, message.methodId, message.invokeId, returnValue, null);
        } catch (Throwable e) {
            reply = new RemoteInvokeMessage(message.name, message.methodId, message.invokeId, null, e);
        }
        
        channel.send(reply, sourceId);
    }
    
    private void checkValidity(Class<?> clazz) {
        Preconditions.checkArgument(clazz.isInterface(), clazz.getName() + " is not an interface");
        Preconditions.checkArgument(Modifier.isPublic(clazz.getModifiers()), clazz.getName() + " is not public");

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (!checkType(TypeToken.of(method.getGenericReturnType()))) {
                throw new IllegalArgumentException("Return type of " + method.getName() + " in " + method.getDeclaringClass().getName() + " is not a sendible type");
            }

            int paramIndex = 0;
            for (Type param : method.getGenericParameterTypes()) {
                if (!checkType(TypeToken.of(param))) {
                    throw new IllegalArgumentException("Parameter " + paramIndex + " (" + param.toString() + ") of " + method.getName() + " in " + method.getDeclaringClass().getName() + " is not a sendible type");
                }
                ++paramIndex;
            }
        }
    }

    private boolean checkType(TypeToken<?> token) {
        if (token.getRawType().equals(Void.TYPE) || token.getRawType().equals(Void.class)) {
            return true;
        }
        
        return Serialization.isSerializable(token);
    }
    
    public class MessageCodec implements ChannelCodec<RemoteInvokeMessage> {
        @Override
        public void encode(RemoteInvokeMessage message, DataOutput out) throws IOException {
            RemoteInterface<?> iface = remotesByName.get(message.name);
            Method method = iface.getMethod(message.methodId);
            
            out.writeUTF(message.name);
            out.writeInt(message.methodId);
            
            message.write(method, out);
        }

        @Override
        public RemoteInvokeMessage decode(DataInput in) throws IOException {
            String name = in.readUTF();
            int methodId = in.readInt();
            
            RemoteInterface<?> iface = remotesByName.get(name);
            if (iface == null) {
                return null;
            }
            
            Method method = iface.getMethod(methodId);
            if (method == null) {
                return null;
            }
            
            RemoteInvokeMessage message = new RemoteInvokeMessage();
            message.name = name;
            message.methodId = methodId;
            message.read(method, in);
            
            return message;
        }
        
    }
}
