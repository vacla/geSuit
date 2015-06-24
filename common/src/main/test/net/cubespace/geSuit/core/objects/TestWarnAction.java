package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;
import net.cubespace.geSuit.core.objects.WarnAction.ActionType;

import org.junit.Test;

public class TestWarnAction {
    @Test
    public void testConstruct1() {
        WarnAction action = new WarnAction(ActionType.IPBan);
        
        assertEquals(ActionType.IPBan, action.getType());
    }
    
    @Test
    public void testConstruct2() {
        WarnAction action = new WarnAction(ActionType.IPBan, 1234);
        
        assertEquals(ActionType.IPBan, action.getType());
        assertEquals(1234, action.getTime());
    }
}
