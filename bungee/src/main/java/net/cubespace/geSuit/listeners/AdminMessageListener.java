package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminMessageListener extends MessageListener {

    public AdminMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.ADMIN_CHANNEL);
    }

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) {
        if (geSuit.getInstance().isDebugEnabled()) {
            Utilities.dumpPacket(event.getTag(), "RECV", event.getData(), true);
        }
        if (eventNotMatched(event)) return;
        //todo any message processing here
    }
}