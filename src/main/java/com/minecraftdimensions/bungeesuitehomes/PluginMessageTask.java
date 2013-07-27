package com.minecraftdimensions.bungeesuitehomes;

import java.io.ByteArrayOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class PluginMessageTask extends BukkitRunnable {
    
    private final BungeeSuiteHomes plugin;
    private final ByteArrayOutputStream bytes;
    private final Player player;
    
    public PluginMessageTask(BungeeSuiteHomes plugin, Player player, ByteArrayOutputStream bytes) {
        this.plugin = plugin;
        this.bytes = bytes;
        this.player = player;
    }

    public void run() {
        player.sendPluginMessage(plugin, BungeeSuiteHomes.OUTGOING_PLUGIN_CHANNEL, bytes.toByteArray());
    }

}