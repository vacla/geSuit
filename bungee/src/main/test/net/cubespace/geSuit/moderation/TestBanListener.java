package net.cubespace.geSuit.moderation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.events.GlobalPlayerPreLoginEvent;

import org.junit.Test;

public class TestBanListener {

    @Test
    public void testNotBanned() {
        BanManager banManager = mock(BanManager.class);
        Logger logger = mock(Logger.class);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        when(banManager.getAnyBan(player)).thenReturn(null);
        
        BanListener listener = new BanListener(banManager, logger);
        
        // Do the test
        GlobalPlayerPreLoginEvent event = new GlobalPlayerPreLoginEvent(player);
        listener.doBanCheck(event);
        
        // Assertions
        assertFalse(event.isCancelled());
    }
    
    @Test
    public void testBanned() {
        BanManager banManager = mock(BanManager.class);
        Logger logger = mock(Logger.class);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        BanInfo<?> ban = new BanInfo<GlobalPlayer>(player);
        ban.setReason("test reason");
        ban.setDate(1000000);
        ban.setUntil(0);
        
        doReturn(ban).when(banManager).getAnyBan(player);
        when(banManager.getBanKickReason(ban)).thenReturn("Ban Reason");
        
        BanListener listener = new BanListener(banManager, logger);
        
        // Do the test
        GlobalPlayerPreLoginEvent event = new GlobalPlayerPreLoginEvent(player);
        listener.doBanCheck(event);
        
        // Assertions
        assertTrue(event.isCancelled());
        assertEquals("Ban Reason", event.getCancelMessage());
        verify(logger).info(anyString());
    }
}
