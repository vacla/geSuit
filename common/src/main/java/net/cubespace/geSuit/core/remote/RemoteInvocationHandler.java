package net.cubespace.geSuit.core.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.messages.RemoteInvokeMessage;

public class RemoteInvocationHandler implements InvocationHandler {
    private String name;
    private Channel<RemoteInvokeMessage> channel;
    private MessageWaiter waiter;
    private Map<Method, Integer> idMap;
    
    private long nextInvokeNum;
    
    RemoteInvocationHandler(String name, Channel<RemoteInvokeMessage> channel, MessageWaiter waiter) {
        this.name = name;
        this.channel = channel;
        this.waiter = waiter;
        
        nextInvokeNum = 0;
    }
    
    void generateReverseMap(Map<Integer, Method> methodMap) {
        idMap = Maps.newHashMap();
        
        for (Entry<Integer, Method> entry : methodMap.entrySet()) {
            idMap.put(entry.getValue(), entry.getKey());
        }
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }
        
        int id = idMap.get(method);
        
        RemoteInvokeMessage message = new RemoteInvokeMessage(name, id, nextInvokeNum++, args);
        Future<Object> future = waiter.waitForReply(message);
        
        channel.broadcast(message);
        
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RemoteTimeoutException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RemoteTimeoutException(e.getMessage());
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
