package com.minecraftdimensions.bungeesuite.managers;

import net.md_5.bungee.api.ProxyServer;

import java.util.logging.Logger;

public class LoggingManager {
    static ProxyServer proxy = ProxyServer.getInstance();
    static Logger log = proxy.getLogger();

    public static void log(String message) {
        log.info(message);
    }

}
