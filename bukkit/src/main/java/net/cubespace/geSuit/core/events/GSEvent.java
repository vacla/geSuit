package net.cubespace.geSuit.core.events;

import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.google.common.collect.Maps;

public abstract class GSEvent extends Event {
    private static Map<Class<?>, HandlerList> handlers = Maps.newIdentityHashMap();
    
    @Override
    public HandlerList getHandlers() {
        return handlers.get(getClass());
    }
    
    protected static Object getHandlerList(Class<?> thisClass) {
        HandlerList list = handlers.get(thisClass);
        if (list == null) {
            list = new HandlerList();
            handlers.put(thisClass, list);
        }
        
        return list;
    }
}
