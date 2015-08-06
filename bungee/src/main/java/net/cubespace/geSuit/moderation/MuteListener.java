package net.cubespace.geSuit.moderation;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MuteListener implements Listener {
    private final MuteManager manager;
    
    public MuteListener(MuteManager manager) {
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer && !event.isCancelled()) {
            event.setCancelled(!manager.checkAllowChat((ProxiedPlayer)event.getSender(), event.isCommand(), event.getMessage()));
        }
    }
}
