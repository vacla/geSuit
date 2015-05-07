package net.cubespace.geSuit.core.remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.Reflection;

public class RemoteInterface<T> {
    private Class<T> interfaceType;
    private T implementation;
    private Map<Integer, Method> methodMap;
    private SetMultimap<Integer, Class<?>> handledExceptions;
    private boolean isRemote;
    
    public RemoteInterface(Class<T> type, T impl) {
        interfaceType = type;
        implementation = impl;
        isRemote = false;
        
        generateMethodMap();
    }
    
    public RemoteInterface(Class<T> type, RemoteInvocationHandler handler) {
        interfaceType = type;
        isRemote = true;
        
        generateMethodMap();
        handler.generateReverseMap(methodMap);
        implementation = Reflection.newProxy(interfaceType, handler);
    }
    
    public Class<T> getType() {
        return interfaceType;
    }
    
    public T get() {
        return implementation;
    }
    
    public boolean isRemote() {
        return isRemote;
    }
    
    Method getMethod(int id) {
        return methodMap.get(id);
    }
    
    Object invoke(int methodId, Object[] params) throws Throwable {
        Preconditions.checkState(!isRemote);
        
        Method method = methodMap.get(methodId);
        if (method == null) {
            throw new InvalidRemoteException("Unknown method by id " + methodId);
        }
        
        try {
            return method.invoke(implementation, params);
        } catch (InvocationTargetException e) {
            if (!isHandledException(methodId, e.getCause())) {
                reportException(method, e.getCause());
            }
            throw e.getCause();
        } catch (Throwable e) {
            if (!isHandledException(methodId, e)) {
                reportException(method, e);
            }
            
            throw e;
        }
    }
    
    private boolean isHandledException(int methodId, Throwable e) {
        // Look for direct declarations
        if (handledExceptions.containsEntry(methodId, e.getClass())) {
            return true;
        }
    
        // Allow subclasses of declarations
        for (Class<?> type : handledExceptions.get(methodId)) {
            if (type.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        
        return false;
    }
    
    private void reportException(Method method, Throwable e) {
        Global.getPlatform().getLogger().log(Level.SEVERE, "An undeclared exception was thrown while executing " + method.toString() + ":", e);
    }
    
    private void generateMethodMap() {
        methodMap = Maps.newHashMap();
        handledExceptions = HashMultimap.create();
        
        Method[] methods = interfaceType.getDeclaredMethods();
        System.out.println("Generating method map for remote " + interfaceType.getName());
        
        // Generate the ids
        for (Method method : methods) {
            // Get the sequence index
            String code = createMethodCode(method);
            int id = code.hashCode() ^ method.getName().hashCode();
            
            System.out.println("- Method " + method.getName() + " code: " + code + " ID: " + id);
            
            methodMap.put(id, method);
            
            // Do handled exceptions
            for (Class<?> type : method.getExceptionTypes()) {
                handledExceptions.put(id, type);
            }
        }
    }
    
    private String createMethodCode(Method method) {
        StringBuilder builder = new StringBuilder();
        
        for (Class<?> param : method.getParameterTypes()) {
            builder.append(createTypeCode(param));
        }
        builder.append(')');
        
        builder.append(createTypeCode(method.getReturnType()));
        
        return builder.toString();
    }
    
    private String createTypeCode(Class<?> type) {
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }
        
        if (type.equals(Integer.class)) {
            return "i";
        } else if (type.equals(Byte.class)) {
            return "b";
        } else if (type.equals(Short.class)) {
            return "s";
        } else if (type.equals(Character.class)) {
            return "c";
        } else if (type.equals(Long.class)) {
            return "l";
        } else if (type.equals(Float.class)) {
            return "f";
        } else if (type.equals(Double.class)) {
            return "d";
        } else if (type.equals(Boolean.class)) {
            return "z";
        } else if (type.equals(Void.class)) {
            return "v";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append('a');
            if (type.isArray()) {
                builder.append('[');
            }
            
            builder.append(type.getName());
            builder.append(';');
            return builder.toString();
        }
    }
}
