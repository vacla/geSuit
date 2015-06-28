package net.cubespace.geSuit.general;

import net.md_5.bungee.api.chat.BaseComponent;

public interface BroadcastHandler {
    public void broadcastOn(String group, BaseComponent[] message);
}
