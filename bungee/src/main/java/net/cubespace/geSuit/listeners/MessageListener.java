package net.cubespace.geSuit.listeners;


import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 31/08/2018.
 */
public abstract class MessageListener implements Listener {
    private static boolean isLegacy = false;
    protected static geSuit.CHANNEL_NAMES channelName;

    public MessageListener(boolean legacy, geSuit.CHANNEL_NAMES channel) {
        channelName = channel;
        isLegacy = legacy;
    }

    /**
     * If true the handler should continue processing the event - but the event will now be cancelled.
     * @param event The event to check
     * @return boolean
     */
    public static boolean eventNotMatched(PluginMessageEvent event) {
        if (event.isCancelled()) return true;
        if (!(event.getSender() instanceof Server)) return true;
        if (event.getTag().equalsIgnoreCase(channelName.toString())
                || (isLegacy && event.getTag().equalsIgnoreCase(channelName.getLegacy()))) {
            if (geSuit.getInstance().isDebugEnabled()) {
                geSuit.getInstance().getLogger().info("Packet Matched and will be handled on channel: " + channelName + " Legacy:" + isLegacy);
            }
            event.setCancelled(true);
            return false;
        }
        return true;
    }
}
