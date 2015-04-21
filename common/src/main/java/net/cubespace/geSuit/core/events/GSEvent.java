package net.cubespace.geSuit.core.events;

public abstract class GSEvent {
    // NOTE: This class is to be overridden by a platform specific implementation when shaded in
    
    protected static Object getHandlerList(Class<?> thisClass) {
        throw new UnsupportedOperationException("This class should have been replaced!");
    }
}
