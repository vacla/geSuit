package com.minecraftdimensions.bungeesuitespawn;

import java.io.ByteArrayOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class PluginMessageTask extends BukkitRunnable {
    
    private final BungeeSuiteSpawn plugin;
    private final ByteArrayOutputStream bytes;
    private final Player player;
    
    public PluginMessageTask(BungeeSuiteSpawn plugin, Player player, ByteArrayOutputStream bytes) {
        this.plugin = plugin;
        this.bytes = bytes;
        this.player = player;
    }

    public void run() {
        player.sendPluginMessage(plugin, BungeeSuiteSpawn.OUTGOING_PLUGIN_CHANNEL, bytes.toByteArray());
    }

}