package net.cubespace.geSuit.core.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.RemoteInvokeMessage;
import net.cubespace.geSuit.core.serialization.Serialization;

public class RemoteManager implements ChannelDataReceiver<BaseMessage> {
    private Channel<BaseMessage> channel;
    private MessageWaiter waiter;
    private Map<Class<?>, RemoteInterface<?>> remotesByClass;
    private Map<String, RemoteInterface<?>> remotesByName;
    
    public RemoteManager(ChannelManager manager) {
        channel = manager.createChannel("remote", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        
        waiter = new MessageWaiter();
        
        remotesByClass = Maps.newHashMap();
        remotesByName = Maps.newHashMap();
    }
    
    public <T> void registerRemote(String name, Class<T> interfaceClass, T implementation) {
        Preconditions.checkArgument(!remotesByClass.containsKey(interfaceClass), "This type is already registered");
        Preconditions.checkArgument(!remotesByName.containsKey(name), "This name is already registered");
        
        checkValidity(interfaceClass);
        
        RemoteInterface<T> handler = new RemoteInterface<T>(interfaceClass, implementation);
        remotesByClass.put(interfaceClass, handler);
        remotesByName.put(name, handler);
    }
    
    public <T> void registerInterest(String name, Class<T> interfaceClass) {
        Preconditions.checkArgument(!remotesByClass.containsKey(interfaceClass), "This type is already registered");
        Preconditions.checkArgument(!remotesByName.containsKey(name), "This name is already registered");
        
        checkValidity(interfaceClass);
        
        RemoteInvocationHandler invocHandler = new RemoteInvocationHandler(name, channel, waiter);
        RemoteInterface<T> remoteHandler = new RemoteInterface<T>(interfaceClass, invocHandler);
        
        remotesByClass.put(interfaceClass, remoteHandler);
        remotesByName.put(name, remoteHandler);
    }
    
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
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value) {
        if (value instanceof RemoteInvokeMessage) {
            RemoteInvokeMessage message = (RemoteInvokeMessage)value;
            
            if (message.isReply()) {
                waiter.checkMessage(message);
            } else {
                onInvoke(message);
            }
        }
    }
    
    private void onInvoke(RemoteInvokeMessage message) {
        RemoteInterface<?> remote = remotesByName.get(message.name);
        if (remote == null) {
            return;
        }
        
        RemoteInvokeMessage reply;
        try {
            Object returnValue = remote.invoke(message.methodId, message.parameters);
            reply = new RemoteInvokeMessage(message.name, message.invokeId, returnValue, null);
        } catch (Throwable e) {
            reply = new RemoteInvokeMessage(message.name, message.invokeId, null, e);
        }
        
        channel.broadcast(reply);
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
}
