package net.cubespace.geSuit.database;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.ProxyServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Created for the AddstarMC Project. Created by Narimm on 22/05/2018.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggingManager.class, ProxyServer.class})
public class TrackingTest extends Tracking {
    
    private String expected;
    
    @Before
    public void Setup() {
        PowerMockito.mockStatic(ProxyServer.class);
        ProxyServer proxy = PowerMockito.mock(ProxyServer.class);
        Logger log = Logger.getGlobal();
        PowerMockito.when(ProxyServer.getInstance()).thenReturn(proxy);
        PowerMockito.when(proxy.getLogger()).thenReturn(log);
        PowerMockito.mockStatic(LoggingManager.class);
    }
    
    @Test
    public void testbatchUpdateNameHistories() {
        List<UUID> uuids = new ArrayList<>();
        uuids.add(Utilities.makeUUID("fdf2d89e-b8f2-4700-b7ff-2e68f359f5d8"));
        expected = "Narimm";
        //uuids.add(Utilities.makeUUID("7463ed26-dfdc-4370-b942-b98807165ff8"));
        //uuids.add(Utilities.makeUUID("8c912b4e-5373-45f0-b7d2-3946ec06a39d"));
        //uuids.add(Utilities.makeUUID("6fe98975-5951-4c15-908c-1a53274002b2"));
        //uuids.add(Utilities.makeUUID("dc2b5c0f-a6fd-40d1-93cd-6be55d429f06"));
        //uuids.add(Utilities.makeUUID("7ef7d6ef-78d3-4c85-b31d-962082cd1af8"));
        //uuids.add(Utilities.makeUUID("55e4cbc1-4ab7-446d-be22-798e98d23d90"));
        //uuids.add(Utilities.makeUUID("f377c61f-4839-49c3-ab9a-f374e9f309be"));
        Runnable runner = nameHistoryUpdater(uuids);
        runner.run();
    }
    
    @Override
    public void insertHistoricTracking(String player, String uuid, String ip, Date changedDate, Date lastSeen) {
        System.out.println("DB update:" + player + " Uuid:" + uuid + " Ip:" + ip + " Changed:" + changedDate + " " +
                "LastSeen: " + lastSeen);
        assertEquals(expected, player);
    }
}
